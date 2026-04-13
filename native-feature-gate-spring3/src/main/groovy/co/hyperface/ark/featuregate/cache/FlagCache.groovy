package co.hyperface.ark.featuregate.cache

import co.hyperface.ark.featuregate.config.FeatureGateProperties
import co.hyperface.ark.featuregate.model.FeatureFlag
import co.hyperface.ark.featuregate.repository.FeatureFlagRepository
import groovy.util.logging.Slf4j
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

import java.util.concurrent.ConcurrentHashMap

@Slf4j
@Component
class FlagCache {

    @Autowired
    private FeatureFlagRepository repository

    @Autowired
    private FeatureGateProperties properties

    private volatile Map<String, FeatureFlag> cache = new ConcurrentHashMap<>()

    @PostConstruct
    void init() {
        refresh()
        log.info("FlagCache initialised with {} flags for environment '{}'", cache.size(), properties.environment)
    }

    @Scheduled(fixedDelayString = '${ark.feature-gate.cache-refresh-interval-ms:30000}')
    void refresh() {
        try {
            List<FeatureFlag> flags = repository.findAllByEnvironmentWithRules(properties.environment)
            Map<String, FeatureFlag> updated = new ConcurrentHashMap<>()
            flags.each { updated[it.flagKey] = it }
            cache = updated
            log.debug("FlagCache refreshed: {} flags loaded", cache.size())
        } catch (Exception e) {
            log.warn("FlagCache refresh failed — serving stale cache ({}): {}", cache.size(), e.getMessage())
        }
    }

    Optional<FeatureFlag> get(String flagKey) {
        return Optional.ofNullable(cache[flagKey])
    }

    // Force immediate refresh after admin mutations
    void forceRefresh() {
        refresh()
    }
}
