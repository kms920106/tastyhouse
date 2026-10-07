package com.tastyhouse.application.rank.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.rank.port.in.RankPeriodManagementListQueryUseCase;
import com.tastyhouse.application.rank.port.out.RankManagementQueryPort;
import com.tastyhouse.application.rank.port.out.RankPeriodResult;

@Service
@Transactional(readOnly = true)
class RankPeriodManagementListQueryService implements RankPeriodManagementListQueryUseCase {

    private final RankManagementQueryPort rankManagementQueryPort;

    public RankPeriodManagementListQueryService(RankManagementQueryPort rankManagementQueryPort) {
        this.rankManagementQueryPort = rankManagementQueryPort;
    }

    @Override
    public List<RankPeriodResult> getPeriods() {
        return rankManagementQueryPort.findAllPeriods();
    }
}
