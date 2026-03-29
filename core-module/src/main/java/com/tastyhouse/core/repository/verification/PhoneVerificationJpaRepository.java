package com.tastyhouse.core.repository.verification;

import com.tastyhouse.core.entity.verification.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhoneVerificationJpaRepository extends JpaRepository<PhoneVerification, Long> {
}
