package co.hyperface.ark.featuregate.cache

import co.hyperface.ark.featuregate.config.FeatureGateProperties
import co.hyperface.ark.featuregate.model.FeatureFlag
import co.hyperface.ark.featuregate.repository.FeatureFlagRepository
import groovy.util.logging.Slf4j
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.annotation.PostConstruct
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

@Slf4j
@Component
class FlagCache {

    @Autowired
    private FeatureFlagRepository repository

    @Autowired
    private FeatureGateProperties properties

    @Autowired(required = false)
    private MeterRegistry meterRegistry

    private final Map<String, CacheEntry> store = new ConcurrentHashMap<>()
    private final Set<String> refreshing = ConcurrentHashMap.newKeySet()

    private static final Executor REFRESH_EXECUTOR = Executors.newFixedThreadPool(2, new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(0)
        @Override
        Thread newThread(Runnable r) {
            Thread t = new Thread(r, "flag-cache-refresh-${count.incrementAndGet()}")
            t.daemon = true
            return t
        }
    })

    @PostConstruct
    void validateConfig() {
        if (properties.cacheTtlMs <= 0) {
            throw new IllegalStateException(
                "FlagCache misconfiguration: cacheTtlMs must be > 0, got ${properties.cacheTtlMs}")
        }
        if (properties.maxStaleMs <= properties.cacheTtlMs) {
            throw new IllegalStateException(
                "FlagCache misconfiguration: maxStaleMs (${properties.maxStaleMs}) must be > cacheTtlMs (${properties.cacheTtlMs}). " +
                "Stale-while-revalidate requires maxStaleMs > cacheTtlMs.")
        }
    }

    Optional<FeatureFlag> get(String flagKey) {
        CacheEntry entry = store[flagKey]
        Instant now = Instant.now()

        if (entry != null) {
            if (entry.loadedAt.plusMillis(properties.cacheTtlMs).isAfter(now)) {
                meterRegistry?.counter('feature.gate.cache', 'result', 'hit')?.increment()
                return Optional.of(entry.flag)                          // fresh — return immediately
            }
            if (entry.loadedAt.plusMillis(properties.maxStaleMs).isAfter(now)) {
                meterRegistry?.counter('feature.gate.cache', 'result', 'stale')?.increment()
                triggerBackgroundRefresh(flagKey)                        // expired but within maxStale
                return Optional.of(entry.flag)                          // serve stale, don't block
            }
        }

        meterRegistry?.counter('feature.gate.cache', 'result', 'miss')?.increment()
        return blockingFetch(flagKey)                                   // cold miss or beyond maxStale
    }

    void invalidate(String flagKey) {
        store.remove(flagKey)
    }

    private void triggerBackgroundRefresh(String flagKey) {
        if (refreshing.add(flagKey)) {                                  // only one refresh per key at a time
            CompletableFuture.runAsync({
                try {
                    blockingFetch(flagKey)
                } finally {
                    refreshing.remove(flagKey)
                }
            }, REFRESH_EXECUTOR)
        }
    }

    private Optional<FeatureFlag> blockingFetch(String flagKey) {
        CacheEntry loaded = store.compute(flagKey) { k, existing ->
            if (existing != null && existing.loadedAt.plusMillis(properties.cacheTtlMs).isAfter(Instant.now())) {
                return existing                                          // another thread already refreshed
            }
            try {
                FeatureFlag flag = repository.findByFlagKey(k).orElse(null)
                return flag ? new CacheEntry(flag: flag, loadedAt: Instant.now()) : null
            } catch (Exception e) {
                log.warn("FlagCache fetch failed for '{}': {}", k, e.getMessage())
                return null                                              // beyond maxStale — fail to false
            }
        }
        return loaded ? Optional.of(loaded.flag) : Optional.empty()
    }

    private static class CacheEntry {
        FeatureFlag flag
        Instant loadedAt
    }
}
