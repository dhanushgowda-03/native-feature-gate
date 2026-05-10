package co.hyperface.ark.featuregate.admin.dto

import co.hyperface.ark.featuregate.model.FlagAuditLog

import java.time.LocalDateTime

class AuditLogResponse {

    Long id
    String flagKey
    String changeType
    String oldValue
    String newValue
    LocalDateTime changedAt

    static AuditLogResponse from(FlagAuditLog log) {
        new AuditLogResponse(
            id: log.id,
            flagKey: log.flagKey,
            changeType: log.changeType,
            oldValue: log.oldValue,
            newValue: log.newValue,
            changedAt: log.changedAt
        )
    }
}
