package com.tastyhouse.core.domain.rank.domain.repository;

import com.tastyhouse.core.domain.rank.application.dto.result.RankDurationResult;
import com.tastyhouse.core.domain.rank.application.dto.result.RankPrizeResult;

import java.util.List;
import java.util.Optional;

public interface RankInfoRepository {

    Optional<RankDurationResult> findActiveDuration();

    List<RankPrizeResult> findActivePrizes();
}
