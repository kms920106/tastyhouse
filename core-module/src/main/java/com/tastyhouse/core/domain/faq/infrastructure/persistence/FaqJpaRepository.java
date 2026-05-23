package com.tastyhouse.core.domain.faq.infrastructure.persistence;

import com.tastyhouse.core.domain.faq.domain.model.Faq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqJpaRepository extends JpaRepository<Faq, Long> {
}
