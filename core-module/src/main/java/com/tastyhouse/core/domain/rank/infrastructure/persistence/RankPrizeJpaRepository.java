package com.tastyhouse.core.domain.rank.infrastructure.persistence;

import com.tastyhouse.core.domain.rank.domain.model.RankPrize;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankPrizeJpaRepository extends JpaRepository<RankPrize, Long> {
}
