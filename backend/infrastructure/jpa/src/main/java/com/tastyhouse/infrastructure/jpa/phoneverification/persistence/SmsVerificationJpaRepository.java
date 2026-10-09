package com.tastyhouse.infrastructure.jpa.phoneverification.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface SmsVerificationJpaRepository extends JpaRepository<SmsVerificationJpaEntity, Long> {
}
