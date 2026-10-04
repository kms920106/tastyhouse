package com.tastyhouse.infrastructure.persistence.faq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqJpaRepository extends JpaRepository<FaqJpaEntity, Long> {
}
