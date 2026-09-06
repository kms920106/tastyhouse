package com.tastyhouse.domain.rank.repository;

import java.util.Optional;

import com.tastyhouse.domain.rank.model.RankPeriod;
import com.tastyhouse.domain.rank.vo.RankPeriodId;

public interface RankPeriodRepository {
    RankPeriod save(RankPeriod rankPeriod);

    Optional<RankPeriod> findById(RankPeriodId id);

    void delete(RankPeriod rankPeriod);
}
