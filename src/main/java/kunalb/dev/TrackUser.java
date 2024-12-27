package kunalb.dev;

import java.io.IOException;
import java.net.URISyntaxException;

public class TrackUser {
    public static void main(String[] args) throws URISyntaxException, IOException {
        // Create objects
        CurlHttpClient curlHttpClient = new CurlHttpClient();
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
        String gitResponse = curlHttpClient.getUserEvents(githubUserName);

        // Filter and summarise User stats
        String userSummary = gitSummaryGenerator.getUserSummary(gitResponse);

        //Clean User Summary
        String cleanSummary = gitSummaryCleaner.cleanGithubSummary(userSummary);
        System.out.println(cleanSummary);

        // Call LLM
        String output = curlHttpClient.getLlmResponse(llmPersona, cleanSummary);

        // Parse LLM output
        String llmOutput = llmParser.extractTextFromLLMOutput(output);
        System.out.println("LLM Output: " + llmOutput);

    }
}
