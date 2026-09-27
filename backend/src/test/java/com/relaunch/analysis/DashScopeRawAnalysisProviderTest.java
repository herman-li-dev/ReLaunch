package com.relaunch.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.alibaba.cloud.ai.dashscope.api.DashScopeResponseFormat;
import com.relaunch.api.AnalyzeRequest;
import com.relaunch.api.BreakReason;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;

class DashScopeRawAnalysisProviderTest {
    @Test
    void buildsTheFrozenJsonOnlyPromptAndUsesSynchronousNoToolOptions() {
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(
                new Generation(new AssistantMessage("{\"skills\":[]}")))));
        DashScopeRawAnalysisProvider provider = new DashScopeRawAnalysisProvider(chatModel,
                new ClassificationRules(Set.of("journal entries"), Set.of("Excel")));
        AnalyzeRequest request = new AnalyzeRequest("r".repeat(200), "j".repeat(100), 24,
                BreakReason.PARENTAL_LEAVE, 5);

        assertThat(provider.analyze(request)).isEqualTo("{\"skills\":[]}");

        ArgumentCaptor<Prompt> prompt = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(prompt.capture());
        assertThat(prompt.getValue().getSystemMessage().getText())
                .contains("Return ONLY valid JSON", "Never invent experience", "resumeQuote must be copied character-for-character");
        assertThat(prompt.getValue().getUserMessage().getText())
                .contains("RESUME:", "TARGET ACCOUNTING JOB:", "ACCOUNTING CLASSIFICATION RULES:", "PLAN RULES:",
                        "BREAK STORY RULES:", "OUTPUT SCHEMA:", "journal entries", "excel");

        var options = (com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions) prompt.getValue().getOptions();
        assertThat(options.getModel()).isEqualTo("qwen-plus");
        assertThat(options.getTemperature()).isEqualTo(0.2);
        assertThat(options.getStream()).isFalse();
        assertThat(options.getTools()).isEmpty();
        assertThat(options.getToolCallbacks()).isEmpty();
        assertThat(options.getResponseFormat().getType()).isEqualTo(DashScopeResponseFormat.Type.JSON_OBJECT);
    }
}
