package com.tastyhouse.core.domain.rank.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.rank.domain.model.RankPeriod;
import com.tastyhouse.core.domain.rank.domain.vo.RankPeriodId;
import com.tastyhouse.core.domain.rank.application.dto.result.RankPeriodResult;

public interface RankPeriodRepository {

    RankPeriod save(RankPeriod rankPeriod);

    Optional<RankPeriod> findById(RankPeriodId id);

    List<RankPeriodResult> findAllPeriods();

    Optional<RankPeriodResult> findPeriodById(RankPeriodId id);

    void delete(RankPeriod rankPeriod);
}
