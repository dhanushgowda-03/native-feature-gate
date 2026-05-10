package co.hyperface.ark.featuregate

import co.hyperface.ark.featuregate.cache.FlagCache
import co.hyperface.ark.featuregate.engine.FlagEvaluationEngine
import co.hyperface.ark.featuregate.model.FlagContext
import groovy.util.logging.Slf4j
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Primary entry point for feature flag evaluation in consuming microservices.
 *
 * Inject and call isEnabled(...) — never throws, always returns false on error.
 *
 * Examples:
 *   featureGate.isEnabled("disbursement-enabled")
 *   featureGate.isEnabled("new-rewards-engine", userId)
 *   featureGate.isEnabled("new-dispute-flow", userId, tenantId)
 *   featureGate.isEnabled("premium-benefits", ["tier": "premium"])
 */
@Slf4j
@Service
class NativeFeatureGate {

    @Autowired
    private FlagCache cache

    @Autowired
    private FlagEvaluationEngine engine

    @Autowired(required = false)
    private MeterRegistry meterRegistry

    boolean isEnabled(String flagKey) {
        evaluate(flagKey, FlagContext.empty())
    }

    boolean isEnabled(String flagKey, String userId) {
        evaluate(flagKey, FlagContext.of(userId))
    }

    boolean isEnabled(String flagKey, String userId, String tenantId) {
        evaluate(flagKey, FlagContext.of(userId, tenantId))
    }

    boolean isEnabled(String flagKey, Map<String, String> properties) {
        evaluate(flagKey, FlagContext.withProperties(properties))
    }

    private boolean evaluate(String flagKey, FlagContext context) {
        if (meterRegistry == null) {
            return evaluateInternal(flagKey, context, null)
        }
        Timer timer = Timer.builder('feature.gate.eval')
            .tag('flagKey', flagKey)
            .register(meterRegistry)
        return timer.recordCallable { evaluateInternal(flagKey, context, meterRegistry) }
    }

    private boolean evaluateInternal(String flagKey, FlagContext context, MeterRegistry registry) {
        try {
            boolean result = cache.get(flagKey)
                .map { flag -> engine.evaluate(flag, context) }
                .orElse(false)
            registry?.counter('feature.gate.eval.result', 'flagKey', flagKey, 'result', result.toString())?.increment()
            return result
        } catch (Exception e) {
            log.error("Error evaluating flag '{}': {}", flagKey, e.getMessage())
            registry?.counter('feature.gate.eval.result', 'flagKey', flagKey, 'result', 'error')?.increment()
            return false
        }
    }
}
