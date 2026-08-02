package com.tastyhouse.infrastructure.faq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqJpaRepository extends JpaRepository<FaqJpaEntity, Long> {
}
