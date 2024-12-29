package dev.kunalb.gitinsight.git;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Component;

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

@Component
public class GitHttpClient {

    Dotenv dotenv = Dotenv.load();

    private static final Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName() );
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public String getUserEvents(String userName) {
        String githubKey = dotenv.get("GITHUB_ACCESS_KEY");
        HttpRequest gitUserRequest = null;
        HttpRequest gitEventsRequest = null;
        try {
            gitUserRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://api.github.com/users/"+userName))
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "kunal-bhadra")
                    .header("Authorization", githubKey)
                    .GET()
                    .build();

            gitEventsRequest = HttpRequest.newBuilder()
                    .uri(new URI("https://api.github.com/users/"+userName+"/events/public?per_page=30"))
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "kunal-bhadra")
                    .header("Authorization", githubKey)
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            LOGGER.severe("GitHub API URI Exception: " + e);
        }


        CompletableFuture<HttpResponse<String>> gitUserAsyncResponse =  client.sendAsync(gitUserRequest, HttpResponse.BodyHandlers.ofString());

        int gitUserStatusCode;
        try {
            gitUserStatusCode = gitUserAsyncResponse.thenApply(HttpResponse::statusCode).get(5, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        if (gitUserStatusCode == HttpURLConnection.HTTP_NOT_FOUND) {
            LOGGER.warning("Invalid GitHub Username");
            return null;
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
            LOGGER.severe("GitHub API Request Status Code:" + gitResultStatusCode);
            LOGGER.severe("GitHub API Request Response Body:" + gitResultBody);
            throw new RuntimeException("GET Request to GitHub API failed");
        }
    }
}
