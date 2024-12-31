package dev.kunalb.gitinsight.git;

import javassist.NotFoundException;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;


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

    public String getRecentEvents(String gitUsername) throws NotFoundException, URISyntaxException, TimeoutException, GitHubGeneralException, GitHubRateLimitExceededException {
        return gitHttpClient.getUserEvents(gitUsername);
    }

    public String getShortSummary(String gitUsername) throws NotFoundException, URISyntaxException, TimeoutException, GitHubGeneralException, GitHubRateLimitExceededException {
        String userEvents = getRecentEvents(gitUsername);
        return gitSummaryGenerator.getUserSummary(userEvents, true);
    }

    public String getLongSummary(String gitUsername) throws NotFoundException, URISyntaxException, TimeoutException, GitHubGeneralException, GitHubRateLimitExceededException {
        String userEvents = getRecentEvents(gitUsername);
        String longSummary = gitSummaryGenerator.getUserSummary(userEvents, false);
        return gitSummaryCleaner.cleanGithubSummary(longSummary);
    }
}
