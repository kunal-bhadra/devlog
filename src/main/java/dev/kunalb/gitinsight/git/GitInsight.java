package dev.kunalb.gitinsight.git;

import org.springframework.stereotype.Service;


@Service
public class GitInsight {
    private final GitHttpClient gitHttpClient;
    private final GitSummaryGenerator gitSummaryGenerator;
    private final GitSummaryCleaner gitSummaryCleaner;


    public GitInsight(GitHttpClient gitHttpClient, GitSummaryGenerator gitSummaryGenerator, GitSummaryCleaner gitSummaryCleaner) {
        this.gitHttpClient = gitHttpClient;
        this.gitSummaryGenerator = gitSummaryGenerator;
        this.gitSummaryCleaner = gitSummaryCleaner;
    }

    public String generateGitSummary(String gitUsername) {

        // Get User's recent events
        String gitResponse = gitHttpClient.getUserEvents(gitUsername);
        if (gitResponse == null) {
            return null;
        }

        // Filter and summarise User stats
        String shortSummary = gitSummaryGenerator.getUserSummary(gitResponse, true);
        String userSummary = gitSummaryGenerator.getUserSummary(gitResponse, false);

        //Clean User Summary
        return shortSummary;
    }
}
