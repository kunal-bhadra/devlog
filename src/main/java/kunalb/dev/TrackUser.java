package kunalb.dev;

import java.net.URISyntaxException;
import java.util.Arrays;

public class TrackUser {
    public static void main(String[] args) throws URISyntaxException {
        GitHttpClient gitHttpClient = new GitHttpClient();
        GitUserSummary gitUserSummary = new GitUserSummary();

        if (args == null) {
            throw new IllegalArgumentException("The GitHub User Name must be provided as an argument.");
        }

        String githubUserName = args[0];
        String gitResponse = gitHttpClient.getUserEvents(githubUserName);

        gitUserSummary.listUserSummary(gitResponse);
    }
}
