package com.tastyhouse.core.domain.partnership.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.partnership.domain.model.PartnershipRequest;

@Repository
public interface PartnershipRequestJpaRepository extends JpaRepository<PartnershipRequest, Long> {
}
