package dev.kunalb.gitinsight.web;

import jakarta.validation.constraints.NotEmpty;

public record GithubUser(
        @NotEmpty
        String githubUsername,
        @NotEmpty
        String llmPersona
) {
}
