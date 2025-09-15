package com.burger.smartblog.service.chat;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeCloudStore;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentCloudReader;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@Slf4j
public class RagService {

    @Resource
    private DashScopeCloudStore dashScopeCloudStore;

    @Resource
    private DashScopeApi dashScopeApi;

    @Value("${rag.index-name}")
    private String indexName;

    /**
     * 将文章内容向量存储到百炼知识库
     *
     * @param articleContent
     */
    public void storeContentToDashScopeCloudStore(String articleContent) {
        dashScopeCloudStore.add(List.of(new Document(articleContent)));
    }

    /**
     * 将文件向量存储到百炼知识库
     */
    public void storeFileToDashScopeCloudStore(byte[] bytes, String originalFileName) {
        log.info("开始将文件向量存储到百炼知识库，文件名为：{}", originalFileName);

        File tempFile = null;
        try {
            // 1. 在系统临时目录创建文件
            String ext = StrUtil.blankToDefault(FileUtil.extName(originalFileName), "tmp");
            tempFile = File.createTempFile(FileUtil.mainName(originalFileName), "." + ext);
            tempFile.deleteOnExit();

            // 2. 写入文件内容
            try (InputStream in = new ByteArrayInputStream(bytes);
                 OutputStream out = new FileOutputStream(tempFile)) {
                IoUtil.copy(in, out);
            }

            log.info("文件已保存到临时目录：{}", tempFile.getAbsolutePath());

            // 3. 读取并解析文档
            DashScopeDocumentCloudReader reader =
                    new DashScopeDocumentCloudReader(tempFile.getAbsolutePath(), dashScopeApi, null);
            List<Document> docs = reader.get();

            for (Document d : docs) {
                d.getMetadata().put("filename", originalFileName);
            }

            dashScopeCloudStore.add(docs);

            log.info("文章id：{}", docs.stream().map(Document::getId).toList());
            log.info("文章：{} 已上传到百炼知识库", originalFileName);
        } catch (Exception e) {
            log.error("文档解析/切分/上传知识库失败", e);
        } finally {
            // 5. 删除临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("临时文件未能删除：{}", originalFileName);
                }
            }
        }
    }


    private String bytesToString(byte[] bytes) {
        // 处理 UTF-8 BOM
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

}
