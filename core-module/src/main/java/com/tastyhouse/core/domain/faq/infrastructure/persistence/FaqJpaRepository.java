package com.tastyhouse.core.domain.faq.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.faq.domain.model.Faq;

public interface FaqJpaRepository extends JpaRepository<Faq, Long> {
}
