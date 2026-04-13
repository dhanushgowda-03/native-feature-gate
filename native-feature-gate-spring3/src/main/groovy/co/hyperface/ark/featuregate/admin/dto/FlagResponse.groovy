package co.hyperface.ark.featuregate.admin.dto

import co.hyperface.ark.featuregate.model.FeatureFlag
import co.hyperface.ark.featuregate.strategy.StrategyType

import java.time.LocalDateTime

class FlagResponse {

    Long id
    String flagKey
    String name
    String description
    boolean enabled
    String environment
    List<RuleResponse> rules
    LocalDateTime createdAt
    LocalDateTime updatedAt

    static FlagResponse from(FeatureFlag flag) {
        new FlagResponse(
            id: flag.id,
            flagKey: flag.flagKey,
            name: flag.name,
            description: flag.description,
            enabled: flag.enabled,
            environment: flag.environment,
            createdAt: flag.createdAt,
            updatedAt: flag.updatedAt,
            rules: flag.rules?.collect { rule ->
                new RuleResponse(id: rule.id, strategy: rule.strategy, parameters: rule.parameters)
            } ?: []
        )
    }

    static class RuleResponse {
        Long id
        StrategyType strategy
        String parameters
    }
}
