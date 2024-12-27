package dev.kunalb.gitinsight.llm;

import org.springframework.stereotype.Service;

@Service
public class LlmInsight {
    public String getLlmSummary(String gitSummary, String llmSummary) {
        LlmHttpClient llmHttpClient = new LlmHttpClient();
        LlmParser llmParser = new LlmParser();

        // Call LLM
        String rawLlmOutput = llmHttpClient.getLlmResponse(llmSummary, gitSummary);

        // Parse LLM output
        return llmParser.extractTextFromLLMOutput(rawLlmOutput);
    }
}
