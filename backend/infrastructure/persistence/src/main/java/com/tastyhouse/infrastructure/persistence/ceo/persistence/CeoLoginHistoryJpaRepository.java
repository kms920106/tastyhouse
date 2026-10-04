package com.tastyhouse.infrastructure.persistence.ceo.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CeoLoginHistoryJpaRepository extends JpaRepository<CeoLoginHistoryJpaEntity, Long> {
}
