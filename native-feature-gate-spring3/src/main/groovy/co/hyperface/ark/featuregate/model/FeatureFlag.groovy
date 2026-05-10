package co.hyperface.ark.featuregate.model

import co.hyperface.ark.featuregate.strategy.StrategyType
import jakarta.persistence.*

import java.time.LocalDateTime

@Entity
@Table(name = "feature_flags", uniqueConstraints = @UniqueConstraint(columnNames = ["flag_key"]))
class FeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id

    @Column(name = "flag_key", nullable = false)
    String flagKey

    @Column(nullable = false)
    String name

    @Column(columnDefinition = "TEXT")
    String description

    @Column(nullable = false)
    boolean enabled = false

    @Enumerated(EnumType.STRING)
    @Column
    StrategyType strategy

    // JSON params: {"userIds":["u1"]} for USER_WHITELIST, {"percentage":10} for PERCENTAGE_ROLLOUT
    @Column(columnDefinition = "TEXT")
    String parameters

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt

    @Column(name = "updated_at", nullable = false)
    LocalDateTime updatedAt

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now()
    }
}
