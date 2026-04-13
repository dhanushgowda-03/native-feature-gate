package co.hyperface.ark.featuregate.strategy

import co.hyperface.ark.featuregate.model.FlagContext
import co.hyperface.ark.featuregate.model.FlagRule
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j

@Slf4j
class UserWhitelistStrategy implements EvaluationStrategy {

    private static final ObjectMapper MAPPER = new ObjectMapper()

    @Override
    boolean evaluate(FlagRule rule, FlagContext context, String flagKey) {
        if (!context.userId) return false
        try {
            Map params = MAPPER.readValue(rule.parameters ?: '{}', Map)
            List<String> userIds = (params.userIds as List<String>) ?: []
            return userIds.contains(context.userId)
        } catch (Exception e) {
            log.warn("Failed to parse USER_WHITELIST parameters for rule {}: {}", rule.id, e.getMessage())
            return false
        }
    }
}
