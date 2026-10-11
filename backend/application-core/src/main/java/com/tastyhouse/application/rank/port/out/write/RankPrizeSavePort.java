package com.tastyhouse.application.rank.port.out.write;

import com.tastyhouse.domain.rank.model.RankPrize;

public interface RankPrizeSavePort {

    RankPrize save(RankPrize rankPrize);

    void delete(RankPrize rankPrize);
}
