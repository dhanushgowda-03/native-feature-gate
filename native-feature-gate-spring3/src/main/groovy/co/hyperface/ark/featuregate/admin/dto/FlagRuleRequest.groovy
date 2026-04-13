package co.hyperface.ark.featuregate.admin.dto

import co.hyperface.ark.featuregate.strategy.StrategyType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.NotNull

@JsonIgnoreProperties(ignoreUnknown = true)
class FlagRuleRequest {

    @NotNull
    StrategyType strategy

    // For USER_WHITELIST:    {"userIds": ["u1", "u2"]}
    // For PERCENTAGE_ROLLOUT: {"percentage": 10}
    // For GLOBAL_ON:          null or {}
    String parameters
}
