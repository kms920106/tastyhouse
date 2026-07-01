package com.tastyhouse.core.domain.rank.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.rank.application.dto.result.RankDurationResult;
import com.tastyhouse.core.domain.rank.application.dto.result.RankPrizeResult;

public interface RankInfoRepository {

    Optional<RankDurationResult> findActiveDuration();

    List<RankPrizeResult> findActivePrizes();
}
