package com.burger.smartblog.ai.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeCloudStore;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeStoreOptions;
import jakarta.annotation.Resource;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量存储配置（采用阿里云百炼云知识库）
 */
@Configuration
public class VectorStoreConfig {

    @Resource
    private DashScopeApi dashScopeApi;

    @Bean
    public VectorStore dashScopeCloudStore() {
        return new DashScopeCloudStore(dashScopeApi, new DashScopeStoreOptions("smart-blog"));
    }

}
