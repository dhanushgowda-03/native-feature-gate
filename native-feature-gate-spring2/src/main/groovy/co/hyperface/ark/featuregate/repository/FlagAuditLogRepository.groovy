package co.hyperface.ark.featuregate.repository

import co.hyperface.ark.featuregate.model.FlagAuditLog
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FlagAuditLogRepository extends JpaRepository<FlagAuditLog, Long> {

    List<FlagAuditLog> findByFlagKeyAndEnvironmentOrderByChangedAtDesc(String flagKey, String environment, Pageable pageable)
}
