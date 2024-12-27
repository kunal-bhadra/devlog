package dev.kunalb.gitinsight;

import dev.kunalb.gitinsight.git.GitHttpClient;
import dev.kunalb.gitinsight.git.GitSummaryCleaner;
import dev.kunalb.gitinsight.git.GitSummaryGenerator;
import dev.kunalb.gitinsight.llm.LlmHttpClient;
import dev.kunalb.gitinsight.llm.LlmParser;
import dev.kunalb.gitinsight.web.GithubUser;
import org.springframework.stereotype.Service;


@Service
public class GitInsight {
    public String run(GithubUser githubUser) {
        // Create objects
        GitHttpClient gitHttpClient = new GitHttpClient();
        LlmHttpClient llmHttpClient = new LlmHttpClient();
        GitSummaryGenerator gitSummaryGenerator = new GitSummaryGenerator();
        GitSummaryCleaner gitSummaryCleaner = new GitSummaryCleaner();
        LlmParser llmParser = new LlmParser();
        String userName = githubUser.githubUsername();
        String llmPersona = githubUser.llmPersona();

        // Get User's recent events
        String gitResponse = gitHttpClient.getUserEvents(userName);

        // Filter and summarise User stats
        String userSummary = gitSummaryGenerator.getUserSummary(gitResponse);

        //Clean User Summary
        String cleanSummary = gitSummaryCleaner.cleanGithubSummary(userSummary);

        // Call LLM
        String output = llmHttpClient.getLlmResponse(llmPersona, cleanSummary);

        // Parse LLM output
        String llmOutput = llmParser.extractTextFromLLMOutput(output);

        return cleanSummary + "\n" + llmOutput;
    }
}
