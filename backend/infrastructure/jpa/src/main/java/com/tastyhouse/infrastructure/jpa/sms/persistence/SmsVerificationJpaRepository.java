package com.tastyhouse.infrastructure.jpa.sms.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SmsVerificationJpaRepository extends JpaRepository<SmsVerificationJpaEntity, Long> {
}
