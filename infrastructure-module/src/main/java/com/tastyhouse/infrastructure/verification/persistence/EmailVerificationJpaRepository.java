package com.tastyhouse.infrastructure.verification.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerificationJpaEntity, Long> {
}
