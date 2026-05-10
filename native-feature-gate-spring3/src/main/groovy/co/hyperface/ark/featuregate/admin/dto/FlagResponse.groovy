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
    StrategyType strategy
    String parameters
    LocalDateTime createdAt
    LocalDateTime updatedAt

    static FlagResponse from(FeatureFlag flag) {
        new FlagResponse(
            id: flag.id,
            flagKey: flag.flagKey,
            name: flag.name,
            description: flag.description,
            enabled: flag.enabled,
            strategy: flag.strategy,
            parameters: flag.parameters,
            createdAt: flag.createdAt,
            updatedAt: flag.updatedAt
        )
    }
}
