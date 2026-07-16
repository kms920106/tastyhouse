package com.tastyhouse.core.domain.faq.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.faq.domain.model.FaqCategory;

public interface FaqCategoryJpaRepository extends JpaRepository<FaqCategory, Long> {
}
