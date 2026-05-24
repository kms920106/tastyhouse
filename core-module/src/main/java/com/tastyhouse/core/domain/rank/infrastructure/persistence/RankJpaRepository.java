package com.tastyhouse.core.domain.rank.infrastructure.persistence;

import com.tastyhouse.core.domain.rank.domain.model.RankPeriod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankJpaRepository extends JpaRepository<RankPeriod, Long> {
}
