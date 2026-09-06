package com.tastyhouse.domain.rank.repository;

import java.util.Optional;

import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPrizeId;

public interface RankPrizeRepository {
    RankPrize save(RankPrize rankPrize);

    Optional<RankPrize> findById(RankPrizeId id);

    void delete(RankPrize rankPrize);
}
