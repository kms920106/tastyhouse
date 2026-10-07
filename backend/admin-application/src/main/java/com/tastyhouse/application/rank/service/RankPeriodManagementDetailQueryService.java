package com.tastyhouse.application.rank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.application.rank.port.in.RankPeriodManagementDetailQueryUseCase;
import com.tastyhouse.application.rank.port.out.RankManagementQueryPort;
import com.tastyhouse.application.rank.port.out.RankPeriodResult;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class RankPeriodManagementDetailQueryService implements RankPeriodManagementDetailQueryUseCase {

    private final RankManagementQueryPort rankManagementQueryPort;

    public RankPeriodManagementDetailQueryService(RankManagementQueryPort rankManagementQueryPort) {
        this.rankManagementQueryPort = rankManagementQueryPort;
    }

    @Override
    public RankPeriodResult getPeriod(Long id) {
        return rankManagementQueryPort.findPeriodById(RankPeriodId.of(id).value())
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.RANK_PERIOD_NOT_FOUND));
    }
}
