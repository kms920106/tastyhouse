package com.tastyhouse.core.domain.verification.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.verification.domain.model.EmailVerification;

@Repository
public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerification, Long> {
}
