package dev.kunalb.gitinsight.llm;

import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

@Service
public class LlmInsight {
    public String getLlmSummary(String gitSummary, String llmSummary) throws URISyntaxException, LlmGeneralException, TimeoutException {
        LlmHttpClient llmHttpClient = new LlmHttpClient();
        LlmParser llmParser = new LlmParser();

        // Call LLM
        String rawLlmOutput = llmHttpClient.getLlmResponse(llmSummary, gitSummary);

        // Parse LLM output
        return llmParser.extractTextFromLLMOutput(rawLlmOutput);
    }
}
