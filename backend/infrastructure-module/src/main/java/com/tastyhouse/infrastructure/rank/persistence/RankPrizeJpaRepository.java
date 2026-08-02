package com.tastyhouse.infrastructure.rank.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RankPrizeJpaRepository extends JpaRepository<RankPrizeJpaEntity, Long> {
}
