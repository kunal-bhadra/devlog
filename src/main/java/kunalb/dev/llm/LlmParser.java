package kunalb.dev.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.logging.Logger;

public class LlmParser {

    private static final Logger LOGGER = Logger.getLogger(
            Thread.currentThread().getStackTrace()[0].getClassName() );

    public String extractTextFromLLMOutput(String jsonString) {
        String ERROR_MESSAGE = "LLM Parser: Invalid Output";
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(jsonString);
            JsonNode candidates = root.get("candidates");

            if (candidates != null && candidates.isArray() && !candidates.isEmpty()) {
                JsonNode firstCandidate = candidates.get(0);
                JsonNode content = firstCandidate.get("content");
                JsonNode parts = content.get("parts");

                if (parts != null && parts.isArray() && !parts.isEmpty()) {
                    JsonNode firstPart = parts.get(0);
                    JsonNode textNode = firstPart.get("text");
                    if (textNode != null && textNode.isTextual()) {
                        return textNode.asText();
                    }else {
                        LOGGER.severe(ERROR_MESSAGE);
                        return null;
                    }
                }else{
                    LOGGER.severe(ERROR_MESSAGE);
                    return null;
                }
            } else {
                LOGGER.severe(ERROR_MESSAGE);
                return null;
            }
        } catch (JsonProcessingException e) {
            LOGGER.severe("Error parsing JSON: " + e.getMessage());
            return null;
        }
    }
}
