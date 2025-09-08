package com.burger.smartblog.service.chat;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeCloudStore;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentCloudReader;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentTransformer;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeStoreOptions;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RagService {

    @Resource
    private ChatClient chatClient;

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
    public void storeFileToDashScopeCloudStore(MultipartFile file) {
        final String original = file.getOriginalFilename();
        final String ext = StrUtil.blankToDefault(FileUtil.extName(original), "tmp");
        final String tmpName = "dashscope_upload_" + IdUtil.fastSimpleUUID() + "." + ext;
        final File tmpDir = FileUtil.getTmpDir();
        final File tmpFile = FileUtil.file(tmpDir, tmpName);
        FileUtil.touch(tmpFile);

        try (InputStream in = file.getInputStream();
             OutputStream out = FileUtil.getOutputStream(tmpFile)) {
            IoUtil.copy(in, out);
        } catch (Exception e) {
            throw new RuntimeException("写入临时文件失败: " + tmpFile.getAbsolutePath(), e);
        }

        try {
            DashScopeDocumentCloudReader reader =
                    new DashScopeDocumentCloudReader(tmpFile.getAbsolutePath(), dashScopeApi, null);
            List<Document> docs = reader.get();

            DashScopeDocumentTransformer transformer = new DashScopeDocumentTransformer(dashScopeApi);
            List<Document> chunks = transformer.apply(docs);

            for (Document d : chunks) {
                d.getMetadata().put("filename", StrUtil.blankToDefault(original, tmpFile.getName()));
            }

            DashScopeCloudStore store =
                    new DashScopeCloudStore(dashScopeApi, new DashScopeStoreOptions(indexName));
            store.add(chunks);

        } catch (Exception e) {
            throw new RuntimeException("文档解析/切分/上传知识库失败", e);
        } finally {
            FileUtil.del(tmpFile);
        }
    }

}
