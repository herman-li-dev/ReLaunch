package com.relaunch.config;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.relaunch.analysis.AnalysisFailedException;
import com.relaunch.analysis.AnalysisResponseProcessor;
import com.relaunch.analysis.AnalysisService;
import com.relaunch.analysis.ClassificationRules;
import com.relaunch.analysis.DashScopeRawAnalysisProvider;
import com.relaunch.analysis.RawAnalysisProvider;
import java.time.Duration;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class ReentryAnalysisConfiguration {
    @Bean
    ClassificationRules classificationRules() {
        return new ClassificationRules(
                Set.of("journal entries", "general ledger", "account reconciliation", "month-end close",
                        "financial statement preparation", "variance analysis", "accounts payable", "accounts receivable",
                        "accruals", "cash-flow analysis", "audit support", "stakeholder communication"),
                Set.of("oracle", "sap", "netsuite", "quickbooks online", "sage", "workday", "erp systems",
                        "excel", "power query", "power bi", "tableau", "automation tools", "reporting systems"));
    }

    @Bean
    AnalysisResponseProcessor analysisResponseProcessor(ObjectMapper objectMapper) {
        return new AnalysisResponseProcessor(objectMapper);
    }

    @Bean
    AnalysisService analysisService(RawAnalysisProvider provider, AnalysisResponseProcessor processor) {
        return new AnalysisService(provider, processor);
    }

    @Bean
    RawAnalysisProvider rawAnalysisProvider(
            @Value("${spring.ai.dashscope.api-key:}") String apiKey,
            @Value("${spring.ai.dashscope.base-url}") String baseUrl,
            @Value("${spring.ai.dashscope.read-timeout:30000}") int readTimeoutMillis,
            ClassificationRules classificationRules) {
        if (apiKey.isBlank()) {
            return request -> { throw new AnalysisFailedException(); };
        }
        return new DashScopeRawAnalysisProvider(chatModel(apiKey, baseUrl, readTimeoutMillis), classificationRules);
    }

    private DashScopeChatModel chatModel(String apiKey, String baseUrl, int readTimeoutMillis) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofMillis(readTimeoutMillis));

        DashScopeApi api = DashScopeApi.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .restClientBuilder(RestClient.builder().requestFactory(requestFactory))
                .webClientBuilder(WebClient.builder())
                .build();
        DashScopeChatOptions defaults = DashScopeChatOptions.builder()
                .withModel("qwen-plus")
                .withTemperature(0.2)
                .withResponseFormat(DashScopeResponseFormat.builder()
                        .type(DashScopeResponseFormat.Type.JSON_OBJECT)
                        .build())
                .withStream(false)
                .withEnableSearch(false)
                .withTools(java.util.List.of())
                .withToolCallbacks(java.util.List.of())
                .withToolNames(Set.of())
                .withInternalToolExecutionEnabled(false)
                .build();
        return DashScopeChatModel.builder().dashScopeApi(api).defaultOptions(defaults).build();
    }
}
