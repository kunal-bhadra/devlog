package dev.kunalb.gitinsight.llm;

public enum LlmPersonaEnum {
    SWE("Senior Software Architect", "SWE"),
    HR("HR Recruiter", "HR"),
    PARTNER("Curious Partner", "PARTNER");

    private final String description;
    private final String code;

    LlmPersonaEnum(String description, String code) {
        this.description = description;
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    public static String fromCode(String code) {
        for (LlmPersonaEnum role : LlmPersonaEnum.values()) {
            if (role.getCode().equalsIgnoreCase(code)) {
                return role.getDescription();
            }
        }
        throw new IllegalArgumentException("No UserRole with code " + code + " found");
    }
}