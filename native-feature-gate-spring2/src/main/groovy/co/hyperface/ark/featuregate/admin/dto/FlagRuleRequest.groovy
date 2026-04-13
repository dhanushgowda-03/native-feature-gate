package co.hyperface.ark.featuregate.admin.dto

import co.hyperface.ark.featuregate.strategy.StrategyType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

import javax.validation.constraints.NotNull

@JsonIgnoreProperties(ignoreUnknown = true)
class FlagRuleRequest {

    @NotNull
    StrategyType strategy

    String parameters
}
