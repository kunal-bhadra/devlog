package kunalb.dev;

import kunalb.dev.git.GitHttpClient;
import kunalb.dev.git.GitSummaryCleaner;
import kunalb.dev.git.GitSummaryGenerator;
import kunalb.dev.llm.LlmHttpClient;
import kunalb.dev.llm.LlmParser;
import org.springframework.boot.SpringApplication;

import java.io.IOException;
import java.net.URISyntaxException;

public class GitInsight {

    public static void main(String[] args) throws URISyntaxException, IOException {
        // Create objects
        GitHttpClient gitHttpClient = new GitHttpClient();
        LlmHttpClient llmHttpClient = new LlmHttpClient();
        GitSummaryGenerator gitSummaryGenerator = new GitSummaryGenerator();
        GitSummaryCleaner gitSummaryCleaner = new GitSummaryCleaner();
        LlmParser llmParser = new LlmParser();

        // Check for valid argument
        if (args == null) {
            throw new IllegalArgumentException("The GitHub User Name must be provided as an argument.");
        }

        // Get User's recent events
        String githubUserName = args[0];
        String llmPersona = args[1];
        String gitResponse = gitHttpClient.getUserEvents(githubUserName);

        // Filter and summarise User stats
        String userSummary = gitSummaryGenerator.getUserSummary(gitResponse);

        //Clean User Summary
        String cleanSummary = gitSummaryCleaner.cleanGithubSummary(userSummary);
        System.out.println(cleanSummary);

        // Call LLM
        String output = llmHttpClient.getLlmResponse(llmPersona, cleanSummary);

        // Parse LLM output
        String llmOutput = llmParser.extractTextFromLLMOutput(output);
        System.out.println("LLM Output: " + llmOutput);

    }
}
