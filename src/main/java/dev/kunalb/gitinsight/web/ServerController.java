package dev.kunalb.gitinsight.web;

import dev.kunalb.gitinsight.git.GitInsight;
import dev.kunalb.gitinsight.git.GitUser;
import dev.kunalb.gitinsight.llm.LlmInsight;
import dev.kunalb.gitinsight.llm.LlmPersona;
import dev.kunalb.gitinsight.llm.LlmPersonaEnum;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.HttpURLConnection;
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
    public String home(Model model) {
        model.addAttribute("gitUser", new GitUser(null));
        return "homepage";
    }

    @PostMapping("/api/git")
    public String getGitSummary(@Valid @ModelAttribute GitUser gitUser, BindingResult result, Model model, HttpSession session, HttpServletResponse response) {

        if (result.hasErrors()) {
            LOGGER.warning("Validation errors: " + result.getAllErrors());
            model.addAttribute("error", "Username cannot be blank.");
            response.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
            return "summary :: error";
        }

        LOGGER.info("Received request: " + gitUser.gitUsername());
        String shortSummary = gitInsight.getShortSummary(gitUser.gitUsername());
        String longSummary = gitInsight.getLongSummary(gitUser.gitUsername());
        model.addAttribute("gitSummary", shortSummary);
        model.addAttribute("llmPersonaCode", new LlmPersona(""));
        session.setAttribute("shortSummary", shortSummary);
        session.setAttribute("longSummary", longSummary);
        return "summary :: summary-list";
    }

    @PostMapping("/api/llm")
    public String getLlmSummary(@ModelAttribute LlmPersona llmPersona, HttpSession session, Model model) {
        String shortSummary = (String) session.getAttribute("shortSummary");
        String longSummary = (String) session.getAttribute("longSummary");
        if (longSummary == null) {
            model.addAttribute("llmResponse", "No summary found, try again.");
            return "git-summary";
        }

        String llmPersonaName = LlmPersonaEnum.fromCode(llmPersona.llmPersonaCode());
        String llmResponse = llmInsight.getLlmSummary(longSummary, llmPersonaName);
        model.addAttribute("llmResponse", llmResponse);
        model.addAttribute("gitSummary", shortSummary);
        return "git-summary";
    }
}
