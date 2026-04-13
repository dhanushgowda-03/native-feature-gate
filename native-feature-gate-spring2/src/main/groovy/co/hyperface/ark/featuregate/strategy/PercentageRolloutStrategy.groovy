package co.hyperface.ark.featuregate.strategy

import co.hyperface.ark.featuregate.model.FlagContext
import co.hyperface.ark.featuregate.model.FlagRule
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j

@Slf4j
class PercentageRolloutStrategy implements EvaluationStrategy {

    private static final ObjectMapper MAPPER = new ObjectMapper()

    @Override
    boolean evaluate(FlagRule rule, FlagContext context, String flagKey) {
        if (!context.userId) return false
        try {
            Map params = MAPPER.readValue(rule.parameters ?: '{}', Map)
            int percentage = (params.percentage as Integer) ?: 0
            if (percentage <= 0) return false
            if (percentage >= 100) return true

            // Deterministic: same userId + flagKey always maps to same bucket
            String hashInput = "${context.userId}:${flagKey}"
            int bucket = Math.abs(hashInput.hashCode()) % 100
            return bucket < percentage
        } catch (Exception e) {
            log.warn("Failed to evaluate PERCENTAGE_ROLLOUT for rule {}: {}", rule.id, e.getMessage())
            return false
        }
    }
}
