package com.tastyhouse.application.rank.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.rank.port.in.RankPrizeQueryUseCase;
import com.tastyhouse.application.rank.port.out.RankPrizeResult;
import com.tastyhouse.application.rank.port.out.RankQueryPort;

@Service
@Transactional(readOnly = true)
class RankPrizeQueryService implements RankPrizeQueryUseCase {

    private final RankQueryPort rankQueryPort;

    public RankPrizeQueryService(RankQueryPort rankQueryPort) {
        this.rankQueryPort = rankQueryPort;
    }

    @Override
    public List<RankPrizeResult> getPrizes() {
        return rankQueryPort.findActivePrizes();
    }
}
