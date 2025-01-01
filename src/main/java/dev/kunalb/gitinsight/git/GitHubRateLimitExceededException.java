package dev.kunalb.gitinsight.git;

import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.time.Instant;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getMessage());
        if (isPrimary) {
            if (retryResetEpochSeconds != null) {
                Instant now = Instant.now();
                Instant retryAfter = Instant.ofEpochSecond(retryResetEpochSeconds);
                Duration retryDuration = Duration.between(now, retryAfter);
                String retryDurationPart = DurationFormatUtils.formatDurationWords(retryDuration.toMillis(), true, true);
                sb.append(". Retry after ").append(retryDurationPart);
            }
        } else {
            if (retryAfterSeconds != null) {
                sb.append(". Retry after ").append(retryAfterSeconds).append(" seconds.");
            } else if (retryResetEpochSeconds != null) {
                Instant now = Instant.now();
                Instant retryAfter = Instant.ofEpochSecond(retryResetEpochSeconds);
                Duration retryDuration = Duration.between(now, retryAfter);
                String retryDurationPart = DurationFormatUtils.formatDurationWords(retryDuration.toMillis(), true, true);
                sb.append(". Retry after ").append(retryDurationPart);
            } else {
                sb.append(". Wait at least one minute before retrying.");
            }
        }
        return sb.toString();
    }
}

