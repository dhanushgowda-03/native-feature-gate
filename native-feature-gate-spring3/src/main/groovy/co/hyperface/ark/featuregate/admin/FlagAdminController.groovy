package co.hyperface.ark.featuregate.admin

import co.hyperface.ark.featuregate.admin.dto.CreateFlagRequest
import co.hyperface.ark.featuregate.admin.dto.FlagResponse
import co.hyperface.ark.featuregate.cache.FlagCache
import co.hyperface.ark.featuregate.config.FeatureGateProperties
import co.hyperface.ark.featuregate.model.FeatureFlag
import co.hyperface.ark.featuregate.model.FlagAuditLog
import co.hyperface.ark.featuregate.model.FlagRule
import co.hyperface.ark.featuregate.repository.FlagAuditLogRepository
import co.hyperface.ark.featuregate.repository.FeatureFlagRepository
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@Slf4j
@RestController
@RequestMapping("/feature-gate/flags")
class FlagAdminController {

    @Autowired private FeatureFlagRepository flagRepository
    @Autowired private FlagAuditLogRepository auditLogRepository
    @Autowired private FlagCache flagCache
    @Autowired private FeatureGateProperties properties

    private final ObjectMapper mapper = new ObjectMapper()

    @PostMapping
    ResponseEntity<FlagResponse> create(@Valid @RequestBody CreateFlagRequest request) {
        if (flagRepository.existsByFlagKeyAndEnvironment(request.flagKey, properties.environment)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Flag '${request.flagKey}' already exists in environment '${properties.environment}'")
        }

        FeatureFlag flag = new FeatureFlag(
            flagKey: request.flagKey,
            name: request.name,
            description: request.description,
            enabled: request.enabled,
            environment: properties.environment
        )

        request.rules?.each { ruleReq ->
            FlagRule rule = new FlagRule(flag: flag, strategy: ruleReq.strategy, parameters: ruleReq.parameters)
            flag.rules.add(rule)
        }

        FeatureFlag saved = flagRepository.save(flag)
        audit(saved.flagKey, "CREATED", null, mapper.writeValueAsString(FlagResponse.from(saved)))
        flagCache.forceRefresh()

        log.info("Flag created: {} in environment {}", saved.flagKey, properties.environment)
        return ResponseEntity.status(HttpStatus.CREATED).body(FlagResponse.from(saved))
    }

    @GetMapping
    ResponseEntity<List<FlagResponse>> list() {
        List<FeatureFlag> flags = flagRepository.findAllByEnvironmentWithRules(properties.environment)
        return ResponseEntity.ok(flags.collect { FlagResponse.from(it) })
    }

    @GetMapping("/{key}")
    ResponseEntity<FlagResponse> get(@PathVariable String key) {
        FeatureFlag flag = findFlag(key)
        return ResponseEntity.ok(FlagResponse.from(flag))
    }

    @PutMapping("/{key}/toggle")
    ResponseEntity<FlagResponse> toggle(@PathVariable String key) {
        FeatureFlag flag = findFlag(key)
        String oldValue = mapper.writeValueAsString(FlagResponse.from(flag))

        flag.enabled = !flag.enabled
        FeatureFlag saved = flagRepository.save(flag)

        audit(key, "TOGGLED", oldValue, mapper.writeValueAsString(FlagResponse.from(saved)))
        flagCache.forceRefresh()

        log.info("Flag '{}' toggled to {} in environment {}", key, saved.enabled, properties.environment)
        return ResponseEntity.ok(FlagResponse.from(saved))
    }

    @DeleteMapping("/{key}")
    ResponseEntity<Void> delete(@PathVariable String key) {
        FeatureFlag flag = findFlag(key)
        String oldValue = mapper.writeValueAsString(FlagResponse.from(flag))

        flagRepository.delete(flag)
        audit(key, "DELETED", oldValue, null)
        flagCache.forceRefresh()

        log.info("Flag '{}' deleted from environment {}", key, properties.environment)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{key}/audit")
    ResponseEntity<List<FlagAuditLog>> auditHistory(@PathVariable String key,
                                                     @RequestParam(defaultValue = "20") int limit) {
        List<FlagAuditLog> logs = auditLogRepository.findByFlagKeyAndEnvironmentOrderByChangedAtDesc(
            key, properties.environment, PageRequest.of(0, limit)
        )
        return ResponseEntity.ok(logs)
    }

    private FeatureFlag findFlag(String key) {
        flagRepository.findByFlagKeyAndEnvironment(key, properties.environment)
            .orElseThrow { new ResponseStatusException(HttpStatus.NOT_FOUND, "Flag '${key}' not found") }
    }

    private void audit(String flagKey, String changeType, String oldValue, String newValue) {
        try {
            auditLogRepository.save(new FlagAuditLog(
                flagKey: flagKey,
                environment: properties.environment,
                changeType: changeType,
                oldValue: oldValue,
                newValue: newValue
            ))
        } catch (Exception e) {
            log.warn("Failed to write audit log for flag '{}': {}", flagKey, e.getMessage())
        }
    }
}
