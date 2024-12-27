package dev.kunalb.llm;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
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

public class LlmHttpClient {

    Dotenv dotenv = Dotenv.load();
    private static final Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName() );
    private static final int HTTP_TOO_MANY_REQUESTS = 429;

    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    public String getLlmResponse(String persona, String summary) throws IOException, URISyntaxException {
        String geminiKey = dotenv.get("GEMINI_ACCESS_KEY");
        String systemPrompt = PromptStore.systemPrompt;
        String inputPrompt = String.format(PromptStore.inputPrompt, persona, summary);

        HttpRequest geminiRequest = HttpRequest.newBuilder()
                .uri(new URI(String.format("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-exp:generateContent?key=%s", geminiKey)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(String.format("{ %s,%s }", systemPrompt, inputPrompt)))
                .build();

        CompletableFuture<HttpResponse<String>> geminiAsyncResponse = client.sendAsync(geminiRequest, HttpResponse.BodyHandlers.ofString());
        int geminiStatusCode;
        String geminiResponse;
        try {
            geminiStatusCode = geminiAsyncResponse.thenApply(HttpResponse::statusCode).get(15, TimeUnit.SECONDS);
            geminiResponse = geminiAsyncResponse.thenApply(HttpResponse::body).get(15, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            String errorMessage = "Gemini API Exception: " + e;
            LOGGER.severe(errorMessage);
            throw new IOException(errorMessage);
        }

        if (geminiStatusCode == HttpURLConnection.HTTP_OK) {
            return geminiResponse;
        } else if (geminiStatusCode == HTTP_TOO_MANY_REQUESTS) {
            LOGGER.severe("Gemini API: Exhausted completely; no more requests");
            throw new IOException("Gemini API: Too many requests");
        } else {
            String errorMessage = "Gemini API: Failed POST request with status code: " + geminiStatusCode;
            LOGGER.severe(errorMessage);
            throw new IOException(errorMessage);
        }
    }
}
