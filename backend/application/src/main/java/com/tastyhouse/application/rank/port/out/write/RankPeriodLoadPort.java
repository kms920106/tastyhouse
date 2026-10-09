package com.tastyhouse.application.rank.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.rank.model.RankPeriod;
import com.tastyhouse.domain.rank.vo.RankPeriodId;

public interface RankPeriodLoadPort {

    Optional<RankPeriod> findActiveById(RankPeriodId id);
}
