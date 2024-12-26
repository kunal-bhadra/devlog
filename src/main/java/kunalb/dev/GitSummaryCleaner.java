package kunalb.dev;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitSummaryCleaner {

    public String cleanGithubSummary(String text) {
        // 1. Remove Hyperlinks
        String noLinks = removeHyperlinks(text);

        // 2. Remove @ Mentions
        String noMentions = removeMentions(noLinks);

        // 3. Remove Code Blocks (both ```...``` and `...`)
        String noCodeBlocks = removeCodeBlocks(noMentions);

        //4. Remove all instances of the word "null"
        String noNulls = removeNulls(noCodeBlocks);

        // 5. Remove any remaining whitespace at the beginning or end of lines that may be introduced
        String trimmedLines = trimLines(noNulls);

        return trimmedLines.trim(); //trim the overall string
    }

    private static String removeHyperlinks(String text) {
        Pattern pattern = Pattern.compile("https?://\\S+");
        Matcher matcher = pattern.matcher(text);
        return matcher.replaceAll("");
    }

    private static String removeMentions(String text) {
        Pattern pattern = Pattern.compile("@\\S+");
        Matcher matcher = pattern.matcher(text);
        return matcher.replaceAll("");
    }

    private static String removeCodeBlocks(String text) {
        //remove multiline code blocks
        Pattern patternMulti = Pattern.compile("```.*?```", Pattern.DOTALL);
        Matcher matcherMulti = patternMulti.matcher(text);
        String temp = matcherMulti.replaceAll("");

        //remove single line code blocks
        Pattern patternSingle = Pattern.compile("`[^`]*?`");
        Matcher matcherSingle = patternSingle.matcher(temp);
        return matcherSingle.replaceAll("");
    }

    private static String removeNulls(String text) {
        Pattern pattern = Pattern.compile("null", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        return matcher.replaceAll("");
    }


    private static String trimLines(String text) {
        Pattern pattern = Pattern.compile("^\\s+|\\s+$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);
        return matcher.replaceAll("");
    }
}
