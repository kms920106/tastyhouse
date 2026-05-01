package com.tastyhouse.core.repository.rank;

import com.tastyhouse.core.entity.rank.dto.RankDurationDto;
import com.tastyhouse.core.entity.rank.dto.RankPrizeDto;

import java.util.List;
import java.util.Optional;

public interface RankRepository {

    Optional<RankDurationDto> findActiveDuration();

    List<RankPrizeDto> findActivePrizes();
}
