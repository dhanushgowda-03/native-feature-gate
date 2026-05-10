package co.hyperface.ark.featuregate.admin.dto

import co.hyperface.ark.featuregate.strategy.StrategyType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper

import javax.validation.constraints.AssertTrue
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Pattern

@JsonIgnoreProperties(ignoreUnknown = true)
class CreateFlagRequest {

    private static final ObjectMapper MAPPER = new ObjectMapper()

    @NotBlank
    @Pattern(regexp = "[a-z0-9\\-_]+", message = "flagKey must contain only lowercase letters, digits, hyphens, or underscores")
    String flagKey

    @NotBlank
    String name

    String description

    boolean enabled = false

    StrategyType strategy

    // For USER_WHITELIST:     {"userIds": ["u1", "u2"]}
    // For PERCENTAGE_ROLLOUT: {"percentage": 10}
    // For GLOBAL_ON / null:   omit or pass null
    String parameters

    @AssertTrue(message = "parameters must be valid JSON")
    boolean isParametersValidJson() {
        if (!parameters) return true
        try {
            MAPPER.readValue(parameters, Map)
            return true
        } catch (Exception ignored) {
            return false
        }
    }
}
