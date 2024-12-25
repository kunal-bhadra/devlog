package kunalb.dev;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class GitUserSummary {

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
            if (pushCommitCount > 0)
                sb.append(String.format("- Pushed %d commit%s to %s\n", pushCommitCount, pushCommitCount > 1 ? "s" : "", repoName));
            if (issueOpenCount > 0)
                sb.append(String.format("- Opened %d issue%s in %s\n", issueOpenCount, issueOpenCount > 1 ? "s" : "", repoName));
            if (starCount > 0)
                sb.append(String.format("- Starred %d repo%s %s\n", starCount, starCount > 1 ? "s" : "", repoName));
            if (pullRequestOpenCount > 0)
                sb.append(String.format("- Opened %d pull request%s in %s\n", pullRequestOpenCount, pullRequestOpenCount > 1 ? "s" : "", repoName));
            if (pullRequestCommentCount > 0)
                sb.append(String.format("- Commented %d time%s on pull requests in %s\n", pullRequestCommentCount, pullRequestCommentCount > 1 ? "s" : "", repoName));
            if (issueCommentCount > 0)
                sb.append(String.format("- Commented on %d issue%s in %s\n", issueCommentCount, issueCommentCount > 1 ? "s" : "", repoName));
            if (memberAddedCount > 0)
                sb.append(String.format("- Added %d member%s to %s\n", memberAddedCount, memberAddedCount > 1 ? "s" : "", repoName));

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
                        String issueBody = payload.path("issue").path("body").asText();
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

    public void listUserSummary(String responseString) {
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
            System.out.println(generateSummary());
        } catch (IOException e) {
            LOGGER.severe("Error: Could not decode JSON response: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.severe("An unexpected error occurred: " + e.getMessage());
        }
    }
}
