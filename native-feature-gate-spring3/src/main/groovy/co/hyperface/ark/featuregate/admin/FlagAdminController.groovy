package co.hyperface.ark.featuregate.admin

import co.hyperface.ark.featuregate.admin.dto.AuditLogResponse
import co.hyperface.ark.featuregate.admin.dto.CreateFlagRequest
import co.hyperface.ark.featuregate.admin.dto.FlagResponse
import co.hyperface.ark.featuregate.admin.dto.UpdateFlagRequest
import co.hyperface.ark.featuregate.cache.FlagCache
import co.hyperface.ark.featuregate.model.FeatureFlag
import co.hyperface.ark.featuregate.model.FlagAuditLog
import co.hyperface.ark.featuregate.repository.FlagAuditLogRepository
import co.hyperface.ark.featuregate.repository.FeatureFlagRepository
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
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

    private final ObjectMapper mapper = new ObjectMapper()

    @PostMapping
    ResponseEntity<FlagResponse> create(@Valid @RequestBody CreateFlagRequest request) {
        FeatureFlag flag = new FeatureFlag(
            flagKey: request.flagKey,
            name: request.name,
            description: request.description,
            enabled: request.enabled,
            strategy: request.strategy,
            parameters: request.parameters
        )

        try {
            FeatureFlag saved = flagRepository.save(flag)
            audit(saved.flagKey, "CREATED", null, mapper.writeValueAsString(FlagResponse.from(saved)))
            flagCache.invalidate(saved.flagKey)
            log.info("Flag created: {}", saved.flagKey)
            return ResponseEntity.status(HttpStatus.CREATED).body(FlagResponse.from(saved))
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Flag '${request.flagKey}' already exists")
        }
    }

    @GetMapping
    ResponseEntity<List<FlagResponse>> list() {
        return ResponseEntity.ok(flagRepository.findAll().collect { FlagResponse.from(it) })
    }

    @GetMapping("/{key}")
    ResponseEntity<FlagResponse> get(@PathVariable String key) {
        return ResponseEntity.ok(FlagResponse.from(findFlag(key)))
    }

    @PutMapping("/{key}")
    ResponseEntity<FlagResponse> update(@PathVariable String key, @Valid @RequestBody UpdateFlagRequest request) {
        FeatureFlag flag = findFlag(key)
        String oldValue = mapper.writeValueAsString(FlagResponse.from(flag))

        flag.name = request.name
        flag.description = request.description
        flag.strategy = request.strategy
        flag.parameters = request.parameters

        FeatureFlag saved = flagRepository.save(flag)
        audit(key, "UPDATED", oldValue, mapper.writeValueAsString(FlagResponse.from(saved)))
        flagCache.invalidate(key)

        log.info("Flag '{}' updated", key)
        return ResponseEntity.ok(FlagResponse.from(saved))
    }

    @PutMapping("/{key}/toggle")
    ResponseEntity<FlagResponse> toggle(@PathVariable String key) {
        FeatureFlag flag = findFlag(key)
        String oldValue = mapper.writeValueAsString(FlagResponse.from(flag))

        flag.enabled = !flag.enabled
        FeatureFlag saved = flagRepository.save(flag)

        audit(key, "TOGGLED", oldValue, mapper.writeValueAsString(FlagResponse.from(saved)))
        flagCache.invalidate(key)

        log.info("Flag '{}' toggled to {}", key, saved.enabled)
        return ResponseEntity.ok(FlagResponse.from(saved))
    }

    @DeleteMapping("/{key}")
    ResponseEntity<Void> delete(@PathVariable String key) {
        FeatureFlag flag = findFlag(key)
        String oldValue = mapper.writeValueAsString(FlagResponse.from(flag))

        flagRepository.delete(flag)
        audit(key, "DELETED", oldValue, null)
        flagCache.invalidate(key)

        log.info("Flag '{}' deleted", key)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{key}/audit")
    ResponseEntity<List<AuditLogResponse>> auditHistory(@PathVariable String key,
                                                         @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(
            auditLogRepository.findByFlagKeyOrderByChangedAtDesc(key, PageRequest.of(0, limit))
                .collect { AuditLogResponse.from(it) }
        )
    }

    private FeatureFlag findFlag(String key) {
        flagRepository.findByFlagKey(key)
            .orElseThrow { new ResponseStatusException(HttpStatus.NOT_FOUND, "Flag '${key}' not found") }
    }

    private void audit(String flagKey, String changeType, String oldValue, String newValue) {
        try {
            auditLogRepository.save(new FlagAuditLog(
                flagKey: flagKey,
                changeType: changeType,
                oldValue: oldValue,
                newValue: newValue
            ))
        } catch (Exception e) {
            log.warn("Failed to write audit log for flag '{}': {}", flagKey, e.getMessage())
        }
    }
}
