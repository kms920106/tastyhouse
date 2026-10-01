package com.tastyhouse.application.rank.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPrizeId;

public interface RankPrizePersistencePort {

    RankPrize save(RankPrize rankPrize);

    Optional<RankPrize> findById(RankPrizeId id);

    void delete(RankPrize rankPrize);
}
