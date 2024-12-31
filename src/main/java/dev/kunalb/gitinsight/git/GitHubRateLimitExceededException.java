package dev.kunalb.gitinsight.git;

public class GitHubRateLimitExceededException extends Exception {
    private final Long retryAfterSeconds;
    private final Long retryResetEpochSeconds;
    private final boolean isPrimary;

    public GitHubRateLimitExceededException(String message, Long retryAfterSeconds, Long retryResetEpochSeconds, boolean isPrimary) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
        this.retryResetEpochSeconds = retryResetEpochSeconds;
        this.isPrimary = isPrimary;
    }

    public Long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    public Long getRetryResetEpochSeconds() {
        return retryResetEpochSeconds;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getMessage());
        if (isPrimary) {
            if (retryResetEpochSeconds != null) {
                sb.append(". Retry after ").append(java.time.Instant.ofEpochSecond(retryResetEpochSeconds));
            }
        } else {
            if (retryAfterSeconds != null) {
                sb.append(". Retry after ").append(retryAfterSeconds).append(" seconds.");
            } else if (retryResetEpochSeconds != null) {
                sb.append(". Retry after ").append(java.time.Instant.ofEpochSecond(retryResetEpochSeconds));
            } else {
                sb.append(". Wait at least one minute before retrying.");
            }
        }
        return sb.toString();
    }
}

