package dev.kunalb.gitinsight.web;

import dev.kunalb.gitinsight.GitInsight;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class ServerController {

    private final GitInsight gitInsight;

    ServerController(GitInsight gitInsight) {
        this.gitInsight = gitInsight;
    }

    @GetMapping("/")
    String root() {
        return "Hi Kunal!";
    }

    @PostMapping("/api/submit")
    String submit(@Valid @RequestBody GithubUser githubUser) {
        return gitInsight.run(githubUser);
    }

}
