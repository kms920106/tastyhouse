package com.tastyhouse.core.domain.rank.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.rank.domain.model.RankPrize;
import com.tastyhouse.core.domain.rank.domain.vo.RankPeriodId;
import com.tastyhouse.core.domain.rank.domain.vo.RankPrizeId;
import com.tastyhouse.core.domain.rank.application.dto.result.RankPrizeManagementResult;

public interface RankPrizeRepository {

    RankPrize save(RankPrize rankPrize);

    Optional<RankPrize> findById(RankPrizeId id);

    List<RankPrizeManagementResult> findByPeriodId(RankPeriodId periodId);

    Optional<RankPrizeManagementResult> findPrizeById(RankPrizeId id);

    void delete(RankPrize rankPrize);
}
