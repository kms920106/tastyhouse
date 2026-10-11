package com.tastyhouse.application.rank.port.out.write;

import com.tastyhouse.domain.rank.model.RankPeriod;

public interface RankPeriodSavePort {

    RankPeriod save(RankPeriod rankPeriod);

    void delete(RankPeriod rankPeriod);
}
