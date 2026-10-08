package com.tastyhouse.infrastructure.persistence.ceo.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface CeoJpaRepository extends JpaRepository<CeoJpaEntity, Long> {
}
