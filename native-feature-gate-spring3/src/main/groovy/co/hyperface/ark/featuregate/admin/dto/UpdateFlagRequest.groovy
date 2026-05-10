package co.hyperface.ark.featuregate.admin.dto

import co.hyperface.ark.featuregate.strategy.StrategyType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.NotBlank

@JsonIgnoreProperties(ignoreUnknown = true)
class UpdateFlagRequest {

    private static final ObjectMapper MAPPER = new ObjectMapper()

    @NotBlank
    String name

    String description

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
