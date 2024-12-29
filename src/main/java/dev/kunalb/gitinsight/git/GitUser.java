package dev.kunalb.gitinsight.git;

import jakarta.validation.constraints.NotEmpty;

public record GitUser(
        @NotEmpty
        String gitUsername
) {
}
