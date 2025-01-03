package dev.kunalb.gitinsight.git;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


@Component
public class GitSummaryGenerator {

    private static final Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName());

    private final Map<String, RepoActivity> activitySummary = new HashMap<>();

    public void updateActivitySummary(JsonNode event) {
        try {
            String eventType = event.path("type").asText();
            String repoName = event.path("repo").path("name").asText();
            if (!activitySummary.containsKey(repoName)) {
                activitySummary.put(repoName, new RepoActivity());
            }

            RepoActivity repoActivity = activitySummary.get(repoName);
            JsonNode payload = event.path("payload");
            String eventAction = payload.path("action").asText();
            switch (eventType) {
                case "WatchEvent":
                    repoActivity.incrementStarCount();
                    break;
                case "IssuesEvent":
                    if ("opened".equals(eventAction)) {
                        repoActivity.incrementIssueOpenCount();
                        String issueTitle = payload.path("issue").path("title").asText();
                        if (!issueTitle.isEmpty()) repoActivity.addIssueOpenTitles(issueTitle);
                    }
                    break;
                case "IssueCommentEvent":
                    if (eventAction.matches("created|edited")) {
                        repoActivity.incrementIssueCommentCount();
                        String issueBody = payload.path("comment").path("body").asText();
                        if (!issueBody.isEmpty()) repoActivity.addIssueCommentMessages(issueBody);
                    }
                    break;
                case "MemberEvent":
                    if ("added".equals(eventAction)) {
                        repoActivity.incrementMemberAddedCount();
                    }
                    break;
                case "PullRequestEvent":
                    if ("opened".equals(eventAction)) {
                        repoActivity.incrementPullRequestOpenCount();
                        String pullRequestReviewOpenTitle = payload.path("pull_request").path("title").asText();
                        if (!pullRequestReviewOpenTitle.isEmpty())
                            repoActivity.addPullRequestOpenTitles(pullRequestReviewOpenTitle);
                    }
                    break;
                case "PullRequestReviewCommentEvent":
                    if ("created".equals(eventAction)) {
                        repoActivity.incrementPullRequestCommentCount();
                        String pullRequestReviewCommentBody = payload.path("comment").path("body").asText();
                        if (!pullRequestReviewCommentBody.isEmpty())
                            repoActivity.addPullRequestComments(pullRequestReviewCommentBody);
                    }
                    break;
                case "PushEvent":
                    int commitCount = payload.path("distinct_size").asInt();
                    repoActivity.incrementPushCount(commitCount);
                    List<JsonNode> commits = new ArrayList<>();
                    payload.get("commits").forEach(commits::add);
                    for (JsonNode commit : commits) {
                        String commitMessage = commit.path("message").asText();
                        if (!commitMessage.isEmpty()) repoActivity.addCommitMessage(commitMessage);
                    }
                    break;
            }
        } catch (Exception e){
            LOGGER.severe("An error occurred processing single event: " + e.getMessage());
        }

    }

    public String generateSummary(boolean shortSummary) throws GitHubGeneralException {
        StringBuilder sb = new StringBuilder();
        for (String repoName : activitySummary.keySet()) {
            RepoActivity repoActivity = activitySummary.get(repoName);
            sb.append(repoActivity.generateSummary(repoName, shortSummary));
        }
        String summary = sb.toString();
        if (summary.trim().isEmpty()) {
            throw new GitHubGeneralException("No public events available for this user");
        }
        return summary;
    }

    public String getUserSummary(String responseString, boolean shortSummary) throws GitHubGeneralException {
        activitySummary.clear();
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(responseString);
            if (root.isArray()) {
                for (JsonNode event : root) {
                    updateActivitySummary(event);
                }
            } else {
                updateActivitySummary(root);
            }
            return generateSummary(shortSummary);
        } catch (IOException e) {
            LOGGER.severe("Error: Could not decode JSON response: " + e.getMessage());
        }
        return "";
    }
}