package co.hyperface.ark.featuregate.repository

import co.hyperface.ark.featuregate.model.FeatureFlag
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {

    @Query("SELECT f FROM FeatureFlag f LEFT JOIN FETCH f.rules WHERE f.environment = :environment")
    List<FeatureFlag> findAllByEnvironmentWithRules(@Param("environment") String environment)

    @Query("SELECT f FROM FeatureFlag f LEFT JOIN FETCH f.rules WHERE f.flagKey = :flagKey AND f.environment = :environment")
    Optional<FeatureFlag> findByFlagKeyAndEnvironment(@Param("flagKey") String flagKey, @Param("environment") String environment)

    boolean existsByFlagKeyAndEnvironment(String flagKey, String environment)
}
