package com.tastyhouse.core.domain.rank.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.rank.domain.model.RankPeriod;

public interface RankPeriodJpaRepository extends JpaRepository<RankPeriod, Long> {
}
