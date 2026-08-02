package com.tastyhouse.infrastructure.faq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FaqCategoryJpaRepository extends JpaRepository<FaqCategoryJpaEntity, Long> {
}
