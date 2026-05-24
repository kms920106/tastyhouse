package com.tastyhouse.core.domain.verification.infrastructure.persistence;

import com.tastyhouse.core.domain.verification.domain.model.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneVerificationJpaRepository extends JpaRepository<PhoneVerification, Long> {
}
