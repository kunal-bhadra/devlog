package dev.kunalb.gitinsight.git;

import javassist.NotFoundException;
import org.springframework.stereotype.Component;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class GitHttpClient {

    private static final int TIMEOUT_SECONDS = 5;
    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public String getUserEvents(String userName) throws NotFoundException, URISyntaxException, TimeoutException, GitHubRateLimitExceededException, GitHubGeneralException {
        String githubKey = System.getenv("GITHUB_ACCESS_KEY");
        HttpRequest gitUserRequest;
        HttpRequest gitEventsRequest;
        try {
            gitUserRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://api.github.com/users/" + userName))
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "kunal-bhadra")
                    .header("Authorization", githubKey)
                    .GET()
                    .build();

            gitEventsRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://api.github.com/users/" + userName + "/events/public?per_page=30"))
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "kunal-bhadra")
                    .header("Authorization", githubKey)
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            throw new URISyntaxException(e.getMessage(), "Bad GitHub API URI");
        }


        CompletableFuture<HttpResponse<String>> gitUserAsyncResponse = client.sendAsync(gitUserRequest, HttpResponse.BodyHandlers.ofString());

        int gitResultStatusCode;
        try {
            gitResultStatusCode = gitUserAsyncResponse.thenApply(HttpResponse::statusCode).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new TimeoutException("GitHub API: User valid check error: " + e.getMessage());
        }
        if (gitResultStatusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new NotFoundException(userName);
        }


        CompletableFuture<HttpResponse<String>> gitEventAsyncResponse = client.sendAsync(gitEventsRequest, HttpResponse.BodyHandlers.ofString());

        String gitResultBody;
        HttpHeaders gitResultHeaders;
        int gitEventsStatusCode;
        try {
            gitResultBody = gitEventAsyncResponse.thenApply(HttpResponse::body).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            gitResultHeaders = gitEventAsyncResponse.thenApply(HttpResponse::headers).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            gitEventsStatusCode = gitEventAsyncResponse.thenApply(HttpResponse::statusCode).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new TimeoutException("GitHub API: User Events Request error: " + e.getMessage());
        }

        if (gitEventsStatusCode == 403 || gitEventsStatusCode == 429) {
            String rateLimitRemaining = gitResultHeaders.firstValue("x-ratelimit-remaining").orElse(null);
            String rateLimitReset = gitResultHeaders.firstValue("x-ratelimit-reset").orElse(null);
            String retryAfter = gitResultHeaders.firstValue("retry-after").orElse(null);

            if ("0".equals(rateLimitRemaining)) {
                assert rateLimitReset != null;
                long resetTimeEpochSeconds = Long.parseLong(rateLimitReset);
                throw new GitHubRateLimitExceededException(
                        "Primary rate limit exceeded",
                        null,
                        resetTimeEpochSeconds,
                        true
                );
            } else if (gitResultBody != null && gitResultBody.contains("secondary rate limit")) {
                Long retryAfterSeconds = retryAfter != null ? Long.parseLong(retryAfter) : null;

                if (retryAfter == null) {
                    retryAfterSeconds = 60L;
                }

                throw new GitHubRateLimitExceededException(
                        "Secondary rate limit exceeded",
                        retryAfterSeconds,
                        null,
                        false
                );
            } else {
                throw new GitHubGeneralException("Received 403/429 without clear rate limit indication: " + gitResultBody);
            }
        } else if (gitEventsStatusCode >= 400 && gitEventsStatusCode < 500) {
            throw new GitHubGeneralException("Client error: " + gitEventsStatusCode + " - " + gitResultBody);
        } else if (gitEventsStatusCode >= 500) {
            throw new GitHubGeneralException("Server error: " + gitEventsStatusCode + " - " + gitResultBody);
        }

        return gitResultBody;
    }
}
