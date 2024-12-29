package dev.kunalb.gitinsight.git;

import java.util.ArrayList;

class RepoActivity {
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

    public String generateSummary(String repoName, boolean shortSummary) {
        StringBuilder sb = new StringBuilder();
        if (starCount > 0)
            sb.append(String.format("Event: Starred, Repo: %s, Count: %d\n", repoName, starCount));
        if (pushCommitCount > 0)
            sb.append(String.format("Event: Pushed, Repo: %s, Count: %d\n", repoName, pushCommitCount));
        if (!pushCommitMessages.isEmpty() && !shortSummary)
            sb.append(String.format("Commit Messages: %s\n", pushCommitMessages));
        if (issueOpenCount > 0)
            sb.append(String.format("Event: Issue Opened, Repo: %s, Count: %d\n", repoName, issueOpenCount));
        if (!issueOpenTitles.isEmpty() && !shortSummary)
            sb.append(String.format("Issue Titles: %s\n", issueOpenTitles));
        if (issueCommentCount > 0)
            sb.append(String.format("Event: Commented, Repo: %s, Count: %d\n", repoName, issueCommentCount));
        if (!issueCommentMessages.isEmpty() && !shortSummary)
            sb.append(String.format("Issue Comments: %s\n", issueCommentMessages));
        if (pullRequestOpenCount > 0)
            sb.append(String.format("Event: PR Opened, Repo: %s, Count: %d\n", repoName, pullRequestOpenCount));
        if (!pullRequestOpenTitles.isEmpty() && !shortSummary)
            sb.append(String.format("PR Titles: %s\n", pullRequestOpenTitles));
        if (pullRequestCommentCount > 0)
            sb.append(String.format("Event: Commented, Repo: %s, Count: %d\n", repoName, pullRequestCommentCount));
        if (!pullRequestComments.isEmpty() && !shortSummary)
            sb.append(String.format("PR Comments: %s\n", pullRequestComments));
        if (memberAddedCount > 0)
            sb.append(String.format("Event: Member Added, Repo: %s, Count: %d\n", repoName, memberAddedCount));

        return sb.toString();
    }

    public void incrementPushCount(int commitCount) {
        pushCommitCount += commitCount;
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

    public void incrementPullRequestCommentCount() {
        pullRequestCommentCount++;
    }

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
