package com.tastyhouse.infrastructure.jpa.faq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface FaqCategoryJpaRepository extends JpaRepository<FaqCategoryJpaEntity, Long> {
}
