package co.hyperface.ark.featuregate.model

import jakarta.persistence.*

import java.time.LocalDateTime

@Entity
@Table(name = "gate_audit_log")
class FlagAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id

    @Column(name = "flag_key", nullable = false)
    String flagKey

    @Column(nullable = false)
    String environment

    @Column(name = "changed_by")
    String changedBy

    // CREATED | UPDATED | TOGGLED | DELETED
    @Column(name = "change_type", nullable = false)
    String changeType

    @Column(name = "old_value", columnDefinition = "TEXT")
    String oldValue

    @Column(name = "new_value", columnDefinition = "TEXT")
    String newValue

    @Column(name = "changed_at", nullable = false, updatable = false)
    LocalDateTime changedAt

    @PrePersist
    void prePersist() {
        changedAt = LocalDateTime.now()
    }
}
