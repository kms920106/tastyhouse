package com.tastyhouse.application.rank.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.rank.model.RankPeriod;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.application.rank.port.out.write.RankPeriodLoadPort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Component
class RankPeriodManagementReader {

    private final RankPeriodLoadPort rankPeriodLoadPort;

    public RankPeriodManagementReader(RankPeriodLoadPort rankPeriodLoadPort) {
        this.rankPeriodLoadPort = rankPeriodLoadPort;
    }

    RankPeriod findPeriodOrThrow(RankPeriodId periodId) {
        return rankPeriodLoadPort.findById(periodId)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.RANK_PERIOD_NOT_FOUND));
    }
}
