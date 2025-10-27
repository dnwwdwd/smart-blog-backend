package com.burger.smartblog.manager;

import com.burger.smartblog.model.vo.upload.UploadBatchFileVo;
import com.burger.smartblog.model.vo.upload.UploadBatchStatusPayload;
import com.burger.smartblog.model.vo.upload.UploadProgressPayload;
import com.burger.smartblog.model.vo.upload.UploadSseMessage;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 管理批量上传的 SSE 连接与事件投递
 */
@Component
@Slf4j
public class UploadBatchManager {

    private static final long SSE_TIMEOUT_MS = Duration.ofMinutes(10).toMillis();
    private static final long CLEANUP_DELAY_SECONDS = 120L;

    private final ConcurrentMap<String, UploadBatchContext> contextMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "upload-batch-cleaner");
        t.setDaemon(true);
        return t;
    });

    public void registerBatch(String batchId, List<UploadBatchFileVo> files) {
        UploadBatchContext context = new UploadBatchContext(batchId);
        context.setFiles(files);
        contextMap.put(batchId, context);
    }

    public void emitFileQueued(String batchId, UploadBatchFileVo fileVo) {
        UploadProgressPayload payload = UploadProgressPayload.builder()
                .batchId(batchId)
                .articleId(fileVo.getArticleId())
                .order(fileVo.getOrder())
                .fileName(fileVo.getFileName())
                .status("queued")
                .message("已进入上传队列")
                .build();
        sendEvent(batchId, "file-progress", payload);
    }

    public void emitFileStatus(String batchId, UploadProgressPayload payload) {
        sendEvent(batchId, "file-progress", payload);
    }

    public void emitBatchStatus(String batchId, String status, String message) {
        UploadBatchStatusPayload payload = UploadBatchStatusPayload.builder()
                .batchId(batchId)
                .status(status)
                .message(message)
                .build();
        sendEvent(batchId, "batch-status", payload);
    }

    public void markBatchCompleted(String batchId) {
        emitBatchStatus(batchId, "completed", "批次已完成");
        markContextCompleted(batchId);
    }

    public void markBatchFailed(String batchId, String message) {
        emitBatchStatus(batchId, "failed", Objects.requireNonNullElse(message, "批次失败"));
        markContextCompleted(batchId);
    }

    private void markContextCompleted(String batchId) {
        UploadBatchContext context = contextMap.get(batchId);
        if (context != null) {
            context.setCompleted(true);
            if (context.getEmitter() != null) {
                try {
                    context.getEmitter().complete();
                } catch (Exception e) {
                    log.debug("complete emitter failed, batchId={}", batchId, e);
                }
            }
            scheduleCleanup(batchId);
        }
    }

    private void scheduleCleanup(String batchId) {
        cleanupExecutor.schedule(() -> {
            UploadBatchContext ctx = contextMap.get(batchId);
            if (ctx == null) {
                return;
            }
            if (ctx.isCompleted()) {
                contextMap.remove(batchId);
            }
        }, CLEANUP_DELAY_SECONDS, TimeUnit.SECONDS);
    }

    @PreDestroy
    public void shutdown() {
        cleanupExecutor.shutdownNow();
    }

    private void sendEvent(String batchId, String event, Object payload) {
        UploadBatchContext context = contextMap.computeIfAbsent(batchId, UploadBatchContext::new);
        UploadSseMessage message = new UploadSseMessage(event, payload);
        context.append(message);
        context.trySend(message);
    }

    public SseEmitter connect(String batchId) {
        UploadBatchContext context = contextMap.computeIfAbsent(batchId, UploadBatchContext::new);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        context.setEmitter(emitter);
        replayHistory(context);

        emitter.onTimeout(() -> {
            log.warn("SSE timeout，batchId={}", batchId);
            cleanup(batchId);
        });
        emitter.onError(e -> {
            log.warn("SSE error，batchId={}", batchId, e);
            cleanup(batchId);
        });
        emitter.onCompletion(() -> cleanup(batchId));
        return emitter;
    }

    private void replayHistory(UploadBatchContext context) {
        SseEmitter emitter = context.getEmitter();
        if (emitter == null) {
            return;
        }
        for (UploadSseMessage msg : context.getHistory()) {
            UploadBatchManager.trySend(emitter, msg);
        }
        if (context.isCompleted()) {
            try {
                emitter.complete();
            } catch (Exception e) {
                log.debug("Complete emitter during replay failed, batchId={}", context.getBatchId(), e);
            }
        }
    }

    private void cleanup(String batchId) {
        UploadBatchContext ctx = contextMap.remove(batchId);
        if (ctx != null && ctx.getEmitter() != null) {
            try {
                ctx.getEmitter().complete();
            } catch (Exception e) {
                log.debug("Cleanup complete emitter failed, batchId={}", batchId, e);
            }
        }
    }

    private static class UploadBatchContext {
        private final String batchId;
        private final CopyOnWriteArrayList<UploadSseMessage> history = new CopyOnWriteArrayList<>();
        private volatile SseEmitter emitter;
        private volatile List<UploadBatchFileVo> files = List.of();
        private final AtomicBoolean completed = new AtomicBoolean(false);

        UploadBatchContext(String batchId) {
            this.batchId = batchId;
        }

        public String getBatchId() {
            return batchId;
        }

        public List<UploadSseMessage> getHistory() {
            return history;
        }

        public void append(UploadSseMessage message) {
            history.add(message);
        }

        public void trySend(UploadSseMessage message) {
            if (emitter != null) {
                UploadBatchManager.trySend(emitter, message);
            }
        }

        public SseEmitter getEmitter() {
            return emitter;
        }

        public void setEmitter(SseEmitter emitter) {
            this.emitter = emitter;
        }

        public void setFiles(List<UploadBatchFileVo> files) {
            this.files = files;
        }

        public List<UploadBatchFileVo> getFiles() {
            return files;
        }

        public boolean isCompleted() {
            return completed.get();
        }

        public void setCompleted(boolean val) {
            this.completed.set(val);
        }
    }

    private static void trySend(SseEmitter target, UploadSseMessage message) {
        try {
            target.send(SseEmitter.event()
                    .name(message.getEvent())
                    .data(message.getData(), MediaType.APPLICATION_JSON));
        } catch (Exception e) {
            log.warn("发送 SSE 事件失败，event={}, error={}", message.getEvent(), e.getMessage());
        }
    }
}
