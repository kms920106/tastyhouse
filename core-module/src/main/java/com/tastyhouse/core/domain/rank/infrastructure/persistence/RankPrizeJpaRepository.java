package com.tastyhouse.core.domain.rank.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tastyhouse.core.domain.rank.domain.model.RankPrize;

public interface RankPrizeJpaRepository extends JpaRepository<RankPrize, Long> {
}
