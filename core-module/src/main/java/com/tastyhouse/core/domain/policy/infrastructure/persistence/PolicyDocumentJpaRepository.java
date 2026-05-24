package com.tastyhouse.core.domain.policy.infrastructure.persistence;

import com.tastyhouse.core.domain.policy.domain.model.PolicyDocument;
import com.tastyhouse.core.domain.policy.domain.model.PolicyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PolicyDocumentJpaRepository extends JpaRepository<PolicyDocument, Long> {

    Optional<PolicyDocument> findByTypeAndCurrent(PolicyType type, Boolean current);

    Optional<PolicyDocument> findByTypeAndVersion(PolicyType type, String version);
}
