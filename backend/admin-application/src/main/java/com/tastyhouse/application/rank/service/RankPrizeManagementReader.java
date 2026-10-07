package com.tastyhouse.application.rank.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPrizeId;
import com.tastyhouse.application.rank.port.out.write.RankPrizePersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Component
class RankPrizeManagementReader {

    private final RankPrizePersistencePort rankPrizePersistencePort;

    public RankPrizeManagementReader(RankPrizePersistencePort rankPrizePersistencePort) {
        this.rankPrizePersistencePort = rankPrizePersistencePort;
    }

    RankPrize findPrizeOrThrow(RankPrizeId prizeId) {
        return rankPrizePersistencePort.findById(prizeId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.RANK_PRIZE_NOT_FOUND));
    }
}
