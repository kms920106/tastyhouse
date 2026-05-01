package com.tastyhouse.core.repository.rank;

import com.tastyhouse.core.entity.rank.Rank;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RankJpaRepository extends JpaRepository<Rank, Long> {
}
