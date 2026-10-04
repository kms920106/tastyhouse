package com.tastyhouse.infrastructure.persistence.rank.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

interface RankPrizeJpaRepository extends JpaRepository<RankPrizeJpaEntity, Long> {
}
