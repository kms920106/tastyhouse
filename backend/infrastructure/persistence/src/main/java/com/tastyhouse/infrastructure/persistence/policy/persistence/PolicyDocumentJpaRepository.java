package com.tastyhouse.infrastructure.persistence.policy.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyDocumentJpaRepository extends JpaRepository<PolicyDocumentJpaEntity, Long> {
}
