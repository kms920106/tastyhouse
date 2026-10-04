package com.tastyhouse.infrastructure.persistence.mail.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface MailVerificationJpaRepository extends JpaRepository<MailVerificationJpaEntity, Long> {
}
