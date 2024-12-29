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

    public String getRecentEvents(String gitUsername) {
        return gitHttpClient.getUserEvents(gitUsername);
    }

    public String getShortSummary(String gitUsername) {
        String userEvents = getRecentEvents(gitUsername);
        return gitSummaryGenerator.getUserSummary(userEvents, true);
    }

    public String getLongSummary(String gitUsername) {
        String userEvents = getRecentEvents(gitUsername);
        String longSummary = gitSummaryGenerator.getUserSummary(userEvents, false);
        return gitSummaryCleaner.cleanGithubSummary(longSummary);
    }
}
