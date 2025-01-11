package dev.kunalb.gitinsight.web;

import dev.kunalb.gitinsight.git.*;
import dev.kunalb.gitinsight.llm.LlmInsight;
import dev.kunalb.gitinsight.llm.LlmPersona;
import dev.kunalb.gitinsight.llm.LlmPersonaEnum;
import dev.kunalb.gitinsight.llm.SummaryNotFoundException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

@Controller
public class ServerController {

    private static final Logger LOGGER = Logger.getLogger(Thread.currentThread().getStackTrace()[0].getClassName());

    private final GitInsight gitInsight;
    private final LlmInsight llmInsight;

    ServerController(GitInsight gitInsight, LlmInsight llmInsight) {
        this.gitInsight = gitInsight;
        this.llmInsight = llmInsight;
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        model.addAttribute("gitUser", new GitUser(null));
        session.setAttribute("longSummary", null);
        return "homepage";
    }

    @PostMapping("/api/git")
    public String getGitSummary(@Valid @ModelAttribute GitUser gitUser, Model model, HttpSession session) throws NotFoundException, URISyntaxException, TimeoutException, GitHubGeneralException, GitHubRateLimitExceededException {
        String shortSummary = gitInsight.getShortSummary(gitUser.gitUsername());
        String longSummary = gitInsight.getLongSummary(gitUser.gitUsername());
        model.addAttribute("gitSummary", shortSummary);
        model.addAttribute("gitUsername", gitUser.gitUsername());
        model.addAttribute("llmPersonaCode", new LlmPersona(""));
        session.setAttribute("longSummary", longSummary);
        LOGGER.info("Completed Git Summary for: " + gitUser.gitUsername());
        return "summary :: summary-list";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public String handleBlankUser(Exception ex, Model model) {
        LOGGER.severe("Username cannot be blank: " + ex.getMessage());
        model.addAttribute("error", "Username cannot be blank");
        return "summary :: error";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({NotFoundException.class})
    public String handleInvalidUser(Exception ex, Model model) {
        LOGGER.severe("GitHub user not found: " + ex.getMessage());
        model.addAttribute("error", "GitHub user not found");
        return "summary :: error";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({URISyntaxException.class})
    public String handleUriException(Exception ex, Model model) {
        LOGGER.severe("GitHub API URL error: " + ex.getMessage());
        model.addAttribute("error", "GitHub API URL error");
        return "summary :: error";
    }

    @ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
    @ExceptionHandler({TimeoutException.class})
    public String handleApiTimeoutException(Exception ex, Model model) {
        LOGGER.severe("Request timed out: " + ex.getMessage());
        model.addAttribute("error", "Request timed out");
        return "summary :: error";
    }

    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    @ExceptionHandler({GitHubRateLimitExceededException.class})
    public String handleGitHubRateLimitException(Exception ex, Model model) {
        LOGGER.severe("GitHub API Rate Limit Exceeded: " + ex.toString());
        model.addAttribute("error", ex.toString());
        return "summary :: error";
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({GitHubGeneralException.class})
    public String handleGitHubGeneralException(Exception ex, Model model) {
        LOGGER.severe(ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "summary :: error";
    }

    @PostMapping("/api/llm")
    public String getLlmSummary(@ModelAttribute LlmPersona llmPersona, HttpSession session, Model model) throws SummaryNotFoundException {
        String longSummary = (String) session.getAttribute("longSummary");
        if (longSummary == null) {
            throw new SummaryNotFoundException("Please generate your Coding Summary before getting your Smart Summary");
        }

        String llmPersonaName = LlmPersonaEnum.fromCode(llmPersona.llmPersonaCode());
        String llmResponse = llmInsight.getLlmSummary(longSummary, llmPersonaName);
        model.addAttribute("llmResponse", llmResponse);
        return "summary :: llm-content";
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({SummaryNotFoundException.class})
    public String handleSummaryNotFoundException(Exception ex, Model model) {
        LOGGER.severe(ex.getMessage());
        model.addAttribute("error", ex.getMessage());
        return "summary :: error";
    }
}
