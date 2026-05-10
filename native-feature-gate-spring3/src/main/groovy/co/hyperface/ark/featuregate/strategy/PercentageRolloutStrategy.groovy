package co.hyperface.ark.featuregate.strategy

import co.hyperface.ark.featuregate.model.FeatureFlag
import co.hyperface.ark.featuregate.model.FlagContext
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j

import java.util.zip.CRC32

@Slf4j
class PercentageRolloutStrategy implements EvaluationStrategy {

    private static final ObjectMapper MAPPER = new ObjectMapper()

    @Override
    boolean evaluate(FeatureFlag flag, FlagContext context) {
        if (!context.userId) return false
        try {
            Map params = MAPPER.readValue(flag.parameters ?: '{}', Map)
            int percentage = (params.percentage as Integer) ?: 0
            if (percentage <= 0) return false
            if (percentage >= 100) return true

            // Deterministic: CRC32 gives stable bucket regardless of JVM implementation
            String hashInput = "${context.userId}:${flag.flagKey}"
            CRC32 crc = new CRC32()
            crc.update(hashInput.getBytes("UTF-8"))
            int bucket = (int)(crc.getValue() % 100)
            return bucket < percentage
        } catch (Exception e) {
            log.warn("Failed to evaluate PERCENTAGE_ROLLOUT for flag {}: {}", flag.flagKey, e.getMessage())
            return false
        }
    }
}
