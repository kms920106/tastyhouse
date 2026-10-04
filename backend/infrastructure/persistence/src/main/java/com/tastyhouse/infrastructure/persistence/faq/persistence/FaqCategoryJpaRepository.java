package com.tastyhouse.infrastructure.persistence.faq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface FaqCategoryJpaRepository extends JpaRepository<FaqCategoryJpaEntity, Long> {
}
