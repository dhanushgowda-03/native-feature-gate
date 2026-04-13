package co.hyperface.ark.featuregate.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "ark.feature-gate")
class FeatureGateProperties {

    // Target environment — only flags for this environment are loaded into cache
    String environment = "default"

    // Cache refresh interval in ms (default 30s). Use ark.featureGate.cacheRefreshIntervalMs to override.
    long cacheRefreshIntervalMs = 30_000
}
