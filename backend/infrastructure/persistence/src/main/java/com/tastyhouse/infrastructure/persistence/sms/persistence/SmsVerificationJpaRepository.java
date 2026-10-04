package com.tastyhouse.infrastructure.persistence.sms.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmsVerificationJpaRepository extends JpaRepository<SmsVerificationJpaEntity, Long> {
}
