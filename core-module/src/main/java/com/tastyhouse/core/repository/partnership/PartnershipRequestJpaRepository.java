package com.tastyhouse.core.repository.partnership;

import com.tastyhouse.core.entity.partnership.PartnershipRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnershipRequestJpaRepository extends JpaRepository<PartnershipRequest, Long> {
}
