package com.tastyhouse.infrastructure.jpa.faq.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface FaqJpaRepository extends JpaRepository<FaqJpaEntity, Long> {
}
