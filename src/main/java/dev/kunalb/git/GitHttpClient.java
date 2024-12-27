package dev.kunalb.git;

import io.github.cdimascio.dotenv.Dotenv;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class GitHttpClient {

    Dotenv dotenv = Dotenv.load();

    private static final Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName() );
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public String getUserEvents(String userName) throws URISyntaxException {
        String githubKey = dotenv.get("GITHUB_ACCESS_KEY");

        HttpRequest gitUserRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/users/"+userName))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("Authorization", githubKey)
                .GET()
                .build();

        HttpRequest gitEventsRequest = HttpRequest.newBuilder()
                .uri(new URI("https://api.github.com/users/"+userName+"/events/public?per_page=100"))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("Authorization", githubKey)
                .GET()
                .build();


        CompletableFuture<HttpResponse<String>> gitUserAsyncResponse =  client.sendAsync(gitUserRequest, HttpResponse.BodyHandlers.ofString());

        int gitUserStatusCode;
        try {
            gitUserStatusCode = gitUserAsyncResponse.thenApply(HttpResponse::statusCode).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        if (gitUserStatusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new IllegalArgumentException("Invalid GitHub Username");
        }


        CompletableFuture<HttpResponse<String>> gitEventAsyncResponse =  client.sendAsync(gitEventsRequest, HttpResponse.BodyHandlers.ofString());

        String gitResultBody = null;
        int gitResultStatusCode = 0;
        try {
            gitResultBody = gitEventAsyncResponse.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
            gitResultStatusCode = gitEventAsyncResponse.thenApply(HttpResponse::statusCode).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.severe("GET Events API Exception: ");
        }

        if (gitResultStatusCode == HttpURLConnection.HTTP_OK) {
            return gitResultBody;
        } else {
            throw new RuntimeException("GET Request to GitHub API failed");
        }
    }
}
