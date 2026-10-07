package com.tastyhouse.application.rank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.vo.RankPrizeId;
import com.tastyhouse.application.rank.port.in.RankPrizeManagementDetailQueryUseCase;
import com.tastyhouse.application.rank.port.out.RankManagementQueryPort;
import com.tastyhouse.application.rank.port.out.RankPrizeManagementResult;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class RankPrizeManagementDetailQueryService implements RankPrizeManagementDetailQueryUseCase {

    private final RankManagementQueryPort rankManagementQueryPort;

    public RankPrizeManagementDetailQueryService(RankManagementQueryPort rankManagementQueryPort) {
        this.rankManagementQueryPort = rankManagementQueryPort;
    }

    @Override
    public RankPrizeManagementResult getPrize(Long prizeId) {
        return rankManagementQueryPort.findPrizeById(RankPrizeId.of(prizeId).value())
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.RANK_PRIZE_NOT_FOUND));
    }
}
