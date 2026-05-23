package com.tastyhouse.core.domain.faq.infrastructure.persistence;

import com.tastyhouse.core.domain.faq.domain.model.FaqCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqCategoryJpaRepository extends JpaRepository<FaqCategory, Long> {
}
