package com.tastyhouse.application.rank.port.in;

import java.util.List;

import com.tastyhouse.application.rank.port.out.RankPrizeResult;

public interface RankPrizeQueryUseCase {

    List<RankPrizeResult> getPrizes();
}
