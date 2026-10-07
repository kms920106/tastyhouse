package com.tastyhouse.application.rank.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.rank.model.RankPeriod;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.application.rank.port.out.write.RankPeriodPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Component
class RankPeriodManagementReader {

    private final RankPeriodPersistencePort rankPeriodPersistencePort;

    public RankPeriodManagementReader(RankPeriodPersistencePort rankPeriodPersistencePort) {
        this.rankPeriodPersistencePort = rankPeriodPersistencePort;
    }

    RankPeriod findPeriodOrThrow(RankPeriodId periodId) {
        return rankPeriodPersistencePort.findById(periodId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.RANK_PERIOD_NOT_FOUND));
    }
}
