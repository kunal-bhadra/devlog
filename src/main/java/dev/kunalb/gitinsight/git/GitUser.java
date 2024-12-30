package dev.kunalb.gitinsight.git;

import jakarta.validation.constraints.NotBlank;

public record GitUser(
        @NotBlank
        String gitUsername
) {
}
