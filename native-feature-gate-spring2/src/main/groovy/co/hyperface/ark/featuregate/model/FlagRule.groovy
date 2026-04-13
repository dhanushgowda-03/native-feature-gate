package co.hyperface.ark.featuregate.model

import co.hyperface.ark.featuregate.strategy.StrategyType

import javax.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "flag_rules")
class FlagRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flag_id", nullable = false)
    FeatureFlag flag

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    StrategyType strategy

    @Column(columnDefinition = "TEXT")
    String parameters

    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now()
    }
}
