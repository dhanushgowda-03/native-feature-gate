package co.hyperface.ark.featuregate

import co.hyperface.ark.featuregate.cache.FlagCache
import co.hyperface.ark.featuregate.engine.FlagEvaluationEngine
import co.hyperface.ark.featuregate.model.FlagContext
import groovy.util.logging.Slf4j
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
        try {
            return cache.get(flagKey)
                    .map { flag -> engine.evaluate(flag, context) }
                    .orElse(false)
        } catch (Exception e) {
            log.error("Error evaluating flag '{}': {}", flagKey, e.getMessage())
            return false
        }
    }
}
