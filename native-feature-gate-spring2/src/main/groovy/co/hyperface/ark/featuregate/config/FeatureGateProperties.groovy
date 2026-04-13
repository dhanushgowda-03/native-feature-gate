package co.hyperface.ark.featuregate.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "ark.feature-gate")
class FeatureGateProperties {

    String environment = "default"
    long cacheRefreshIntervalMs = 30_000
}
