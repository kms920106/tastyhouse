package com.tastyhouse.core.domain.partnership.infrastructure.persistence;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PartnershipRequestJpaRepository extends JpaRepository<PartnershipRequest, Long> {
}
