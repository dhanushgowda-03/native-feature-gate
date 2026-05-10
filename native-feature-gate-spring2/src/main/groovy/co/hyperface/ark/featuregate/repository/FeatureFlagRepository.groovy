package co.hyperface.ark.featuregate.repository

import co.hyperface.ark.featuregate.model.FeatureFlag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {

    Optional<FeatureFlag> findByFlagKey(String flagKey)

    boolean existsByFlagKey(String flagKey)
}
