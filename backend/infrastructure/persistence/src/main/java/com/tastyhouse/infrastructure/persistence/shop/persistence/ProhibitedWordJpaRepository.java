package com.tastyhouse.infrastructure.persistence.shop.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProhibitedWordJpaRepository extends JpaRepository<ProhibitedWordJpaEntity, Long> {
}
