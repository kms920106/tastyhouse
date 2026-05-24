package com.tastyhouse.core.domain.rank.infrastructure.persistence;

import com.tastyhouse.core.domain.rank.domain.model.Rank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankJpaRepository extends JpaRepository<Rank, Long> {
}
