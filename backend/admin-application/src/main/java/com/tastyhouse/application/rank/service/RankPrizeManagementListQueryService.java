package com.tastyhouse.application.rank.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.application.rank.port.in.RankPrizeManagementListQueryUseCase;
import com.tastyhouse.application.rank.port.out.RankManagementQueryPort;
import com.tastyhouse.application.rank.port.out.RankPrizeManagementResult;

@Service
@Transactional(readOnly = true)
class RankPrizeManagementListQueryService implements RankPrizeManagementListQueryUseCase {

    private final RankManagementQueryPort rankManagementQueryPort;

    public RankPrizeManagementListQueryService(RankManagementQueryPort rankManagementQueryPort) {
        this.rankManagementQueryPort = rankManagementQueryPort;
    }

    @Override
    public List<RankPrizeManagementResult> getPrizesByPeriod(Long periodId) {
        return rankManagementQueryPort.findPrizesByPeriodId(RankPeriodId.of(periodId).value());
    }
}
