package co.hyperface.ark.featuregate.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "ark.feature-gate")
class FeatureGateProperties {

    // How long a cache entry is considered fresh. Override: ark.feature-gate.cache-ttl-ms
    long cacheTtlMs = 120_000

    // Max age beyond which stale data is not served — blocking fetch attempted instead.
    // Override: ark.feature-gate.max-stale-ms
    long maxStaleMs = 3_600_000
}
