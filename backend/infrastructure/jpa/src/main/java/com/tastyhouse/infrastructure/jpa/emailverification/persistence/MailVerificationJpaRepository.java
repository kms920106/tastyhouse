package com.tastyhouse.infrastructure.jpa.emailverification.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface MailVerificationJpaRepository extends JpaRepository<MailVerificationJpaEntity, Long> {
}
