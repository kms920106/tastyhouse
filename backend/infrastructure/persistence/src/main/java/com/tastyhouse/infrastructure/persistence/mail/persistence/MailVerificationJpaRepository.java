package com.tastyhouse.infrastructure.persistence.mail.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailVerificationJpaRepository extends JpaRepository<MailVerificationJpaEntity, Long> {
}
