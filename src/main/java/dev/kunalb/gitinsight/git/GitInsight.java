package dev.kunalb.gitinsight.git;

import org.springframework.stereotype.Service;


@Service
public class GitInsight {
    public String gitSummary = "";

    public String getGitSummary() {
        return gitSummary;
    }

    public String generateGitSummary(String gitUsername) {

        // Create objects
        GitHttpClient gitHttpClient = new GitHttpClient();
        GitSummaryGenerator gitSummaryGenerator = new GitSummaryGenerator();
        GitSummaryCleaner gitSummaryCleaner = new GitSummaryCleaner();

        // Get User's recent events
        String gitResponse = gitHttpClient.getUserEvents(gitUsername);
        if (gitResponse == null) {
            return null;
        }

        // Filter and summarise User stats
        String shortSummary = gitSummaryGenerator.getUserSummary(gitResponse, true);
        String userSummary = gitSummaryGenerator.getUserSummary(gitResponse, false);

        //Clean User Summary
        this.gitSummary = gitSummaryCleaner.cleanGithubSummary(userSummary);
        return shortSummary;
    }
}
