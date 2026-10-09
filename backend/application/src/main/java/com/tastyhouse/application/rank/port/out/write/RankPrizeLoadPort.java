package com.tastyhouse.application.rank.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPrizeId;

public interface RankPrizeLoadPort {

    Optional<RankPrize> findActiveById(RankPrizeId id);
}
