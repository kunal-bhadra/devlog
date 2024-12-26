package kunalb.dev;

import java.net.URISyntaxException;

public class TrackUser {
    public static void main(String[] args) throws URISyntaxException {
        // Create objects
        CurlHttpClient curlHttpClient = new CurlHttpClient();
        GitSummaryGenerator gitSummaryGenerator = new GitSummaryGenerator();
        GitSummaryCleaner gitSummaryCleaner = new GitSummaryCleaner();

        // Check for valid argument
        if (args == null) {
            throw new IllegalArgumentException("The GitHub User Name must be provided as an argument.");
        }

        // Get User's recent events
        String githubUserName = args[0];
        String gitResponse = curlHttpClient.getUserEvents(githubUserName);

        // Filter and summarise User stats
        String userSummary = gitSummaryGenerator.getUserSummary(gitResponse);

        //Clean User Summary
        String cleanSummary = gitSummaryCleaner.cleanGithubSummary(userSummary);
        System.out.println(cleanSummary);

        // Call LLM
        String output = curlHttpClient.getLlmResponse(cleanSummary);
        System.out.println(output);
    }
}
