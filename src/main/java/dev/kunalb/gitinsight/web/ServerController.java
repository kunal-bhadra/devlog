package dev.kunalb.gitinsight.web;

import dev.kunalb.gitinsight.git.GitInsight;
import dev.kunalb.gitinsight.git.GitUser;
import dev.kunalb.gitinsight.git.UserNotFoundException;
import dev.kunalb.gitinsight.llm.LlmInsight;
import dev.kunalb.gitinsight.llm.LlmPersona;
import dev.kunalb.gitinsight.llm.LlmPersonaEnum;
import jakarta.validation.Valid;
import org.apache.catalina.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
public class ServerController {

    private final GitInsight gitInsight;
    private final LlmInsight llmInsight;

    ServerController(GitInsight gitInsight, LlmInsight llmInsight) {
        this.gitInsight = gitInsight;
        this.llmInsight = llmInsight;
    }


    @GetMapping("/")
    String root() {
        return "Hi Kunal!";
    }

    @PostMapping("/api/git")
    public String getGitSummary(@Valid @RequestBody GitUser gitUser) {
        String gitSummary = gitInsight.generateGitSummary(gitUser.gitUsername());
        if (gitSummary == null) {
            throw new UserNotFoundException();
        }
        return gitSummary;
    }

    @PostMapping("/api/llm")
    String getLlmSummary(@Valid @RequestBody LlmPersona llmPersonaCode) {
        String llmPersona = LlmPersonaEnum.fromCode(llmPersonaCode.llmPersonaCode());
        return llmInsight.getLlmSummary(gitInsight.getGitSummary(), llmPersona);
    }

}
