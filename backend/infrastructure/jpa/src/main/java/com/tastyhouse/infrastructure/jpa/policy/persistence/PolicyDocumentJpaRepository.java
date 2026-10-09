package com.tastyhouse.infrastructure.jpa.policy.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface PolicyDocumentJpaRepository extends JpaRepository<PolicyDocumentJpaEntity, Long> {
}
