package co.hyperface.ark.featuregate.engine

import co.hyperface.ark.featuregate.model.FeatureFlag
import co.hyperface.ark.featuregate.model.FlagContext
import co.hyperface.ark.featuregate.strategy.EvaluationStrategy
import co.hyperface.ark.featuregate.strategy.GlobalStrategy
import co.hyperface.ark.featuregate.strategy.PercentageRolloutStrategy
import co.hyperface.ark.featuregate.strategy.StrategyType
import co.hyperface.ark.featuregate.strategy.UserWhitelistStrategy
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FlagEvaluationEngine {

    @Autowired(required = false)
    private MeterRegistry meterRegistry

    private final Map<StrategyType, EvaluationStrategy> strategies = [
        (StrategyType.GLOBAL_ON)           : new GlobalStrategy(),
        (StrategyType.USER_WHITELIST)       : new UserWhitelistStrategy(),
        (StrategyType.PERCENTAGE_ROLLOUT)   : new PercentageRolloutStrategy()
    ]

    boolean evaluate(FeatureFlag flag, FlagContext context) {
        if (!flag.enabled) return false
        if (!flag.strategy) {
            meterRegistry?.counter('feature.gate.strategy', 'type', 'none')?.increment()
            return true
        }
        EvaluationStrategy strategy = strategies[flag.strategy]
        if (!strategy) {
            meterRegistry?.counter('feature.gate.strategy', 'type', 'unknown')?.increment()
            return false
        }
        meterRegistry?.counter('feature.gate.strategy', 'type', flag.strategy.name())?.increment()
        return strategy.evaluate(flag, context)
    }
}
