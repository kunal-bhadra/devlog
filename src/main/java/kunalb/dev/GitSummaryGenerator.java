package kunalb.dev;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class GitSummaryGenerator {

    private static final Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName());

    private static final Map<String, RepoActivity> activitySummary = new HashMap<>();

    static class RepoActivity {
        int starCount = 0;
        int pushCommitCount = 0;
        ArrayList<String> pushCommitMessages = new ArrayList<>();
        int issueOpenCount = 0;
        ArrayList<String> issueOpenTitles = new ArrayList<>();
        int issueCommentCount = 0;
        ArrayList<String> issueCommentMessages = new ArrayList<>();
        int pullRequestOpenCount = 0;
        ArrayList<String> pullRequestOpenTitles = new ArrayList<>();
        int pullRequestCommentCount = 0;
        ArrayList<String> pullRequestComments = new ArrayList<>();
        int memberAddedCount = 0;

        public String generateSummary(String repoName) {
            StringBuilder sb = new StringBuilder();
            if (starCount > 0)
                sb.append(String.format("Event: Starred, Repo: %s, Count: %d\n", repoName, starCount));
            if (pushCommitCount > 0)
                sb.append(String.format("Event: Pushed, Repo: %s, Count: %d\n", repoName, pushCommitCount));
            if (!pushCommitMessages.isEmpty())
                sb.append(String.format("Commit Messages: %s\n", pushCommitMessages));
            if (issueOpenCount > 0)
                sb.append(String.format("Event: Issue Opened, Repo: %s, Count: %d\n", repoName, issueOpenCount));
            if (!issueOpenTitles.isEmpty())
                sb.append(String.format("Issue Titles: %s\n", issueOpenTitles));
            if (issueCommentCount > 0)
                sb.append(String.format("Event: Commented, Repo: %s, Count: %d\n", repoName, issueCommentCount));
            if (!issueCommentMessages.isEmpty())
                sb.append(String.format("Issue Comments: %s\n", issueCommentMessages));
            if (pullRequestOpenCount > 0)
                sb.append(String.format("Event: PR Opened, Repo: %s, Count: %d\n", repoName, pullRequestOpenCount));
            if (!pullRequestOpenTitles.isEmpty())
                sb.append(String.format("PR Titles: %s\n", pullRequestOpenTitles));
            if (pullRequestCommentCount > 0)
                sb.append(String.format("Event: Commented, Repo: %s, Count: %d\n", repoName, pullRequestCommentCount));
            if (!pullRequestComments.isEmpty())
                sb.append(String.format("PR Comments: %s\n", pullRequestComments));
            if (memberAddedCount > 0)
                sb.append(String.format("Event: Member Added, Repo: %s, Count: %d\n", repoName, memberAddedCount));

            return sb.toString();
        }

        public void incrementPushCount(int commitCount) {
            pushCommitCount+=commitCount;
        }

        public void addCommitMessage(String message) {
            pushCommitMessages.add(message);
        }

        public void incrementIssueOpenCount() {
            issueOpenCount++;
        }

        public void addIssueOpenTitles(String message) {
            issueOpenTitles.add(message);
        }

        public void incrementStarCount() {
            starCount++;
        }

        public void incrementPullRequestOpenCount() {
            pullRequestOpenCount++;
        }

        public void incrementPullRequestCommentCount() { pullRequestCommentCount++; }

        public void addPullRequestComments(String message) {
            pullRequestComments.add(message);
        }

        public void addPullRequestOpenTitles(String message) {
            pullRequestOpenTitles.add(message);
        }

        public void incrementIssueCommentCount() {
            issueCommentCount++;
        }

        public void addIssueCommentMessages(String message) {
            issueCommentMessages.add(message);
        }

        public void incrementMemberAddedCount() {
            memberAddedCount++;
        }

    }

    public static void updateActivitySummary(JsonNode event) {
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

    public static String generateSummary() {
        StringBuilder sb = new StringBuilder();
        for (String repoName : activitySummary.keySet()) {
            RepoActivity repoActivity = activitySummary.get(repoName);
            sb.append(repoActivity.generateSummary(repoName));
        }
        return sb.toString();
    }

    public String getUserSummary(String responseString) {
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
            return generateSummary();
        } catch (IOException e) {
            LOGGER.severe("Error: Could not decode JSON response: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("An unexpected error occurred: " + e.getMessage());
        }
        return responseString;
    }
}
