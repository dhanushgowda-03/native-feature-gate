package co.hyperface.ark.featuregate.engine

import co.hyperface.ark.featuregate.model.FeatureFlag
import co.hyperface.ark.featuregate.model.FlagContext
import co.hyperface.ark.featuregate.strategy.EvaluationStrategy
import co.hyperface.ark.featuregate.strategy.GlobalStrategy
import co.hyperface.ark.featuregate.strategy.PercentageRolloutStrategy
import co.hyperface.ark.featuregate.strategy.StrategyType
import co.hyperface.ark.featuregate.strategy.UserWhitelistStrategy
import org.springframework.stereotype.Component

@Component
class FlagEvaluationEngine {

    private final Map<StrategyType, EvaluationStrategy> strategies = [
        (StrategyType.GLOBAL_ON)           : new GlobalStrategy(),
        (StrategyType.USER_WHITELIST)       : new UserWhitelistStrategy(),
        (StrategyType.PERCENTAGE_ROLLOUT)   : new PercentageRolloutStrategy()
    ]

    boolean evaluate(FeatureFlag flag, FlagContext context) {
        if (!flag.enabled) return false

        // No rules = flag is globally on for everyone
        if (!flag.rules || flag.rules.isEmpty()) return true

        // Any rule match = enabled for this context
        return flag.rules.any { rule ->
            EvaluationStrategy strategy = strategies[rule.strategy]
            strategy ? strategy.evaluate(rule, context, flag.flagKey) : false
        }
    }
}
