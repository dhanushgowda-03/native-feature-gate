package co.hyperface.ark.featuregate.model

import javax.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "feature_flags",
    uniqueConstraints = @UniqueConstraint(columnNames = ["flag_key", "environment"])
)
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

    @Column(nullable = false)
    String environment

    @OneToMany(mappedBy = "flag", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    List<FlagRule> rules = []

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
