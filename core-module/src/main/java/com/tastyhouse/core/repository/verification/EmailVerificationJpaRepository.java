package com.tastyhouse.core.repository.verification;

import com.tastyhouse.core.entity.verification.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmailVerificationJpaRepository extends JpaRepository<EmailVerification, Long> {
}
