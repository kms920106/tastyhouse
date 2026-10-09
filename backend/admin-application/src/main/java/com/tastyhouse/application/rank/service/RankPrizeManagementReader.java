package com.tastyhouse.application.rank.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPrizeId;
import com.tastyhouse.application.rank.port.out.write.RankPrizeLoadPort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Component
class RankPrizeManagementReader {

    private final RankPrizeLoadPort rankPrizeLoadPort;

    public RankPrizeManagementReader(RankPrizeLoadPort rankPrizeLoadPort) {
        this.rankPrizeLoadPort = rankPrizeLoadPort;
    }

    RankPrize findPrizeOrThrow(RankPrizeId prizeId) {
        return rankPrizeLoadPort.findById(prizeId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.RANK_PRIZE_NOT_FOUND));
    }
}
