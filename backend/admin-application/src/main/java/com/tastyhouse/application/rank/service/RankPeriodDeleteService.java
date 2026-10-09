package com.tastyhouse.application.rank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.model.RankPeriod;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.application.rank.port.in.RankPeriodDeleteCommand;
import com.tastyhouse.application.rank.port.in.RankPeriodDeleteUseCase;
import com.tastyhouse.application.rank.port.out.write.RankPeriodSavePort;

@Service
@Transactional
class RankPeriodDeleteService implements RankPeriodDeleteUseCase {

    private final RankPeriodSavePort rankPeriodSavePort;
    private final RankPeriodManagementReader rankPeriodManagementReader;

    public RankPeriodDeleteService(
        RankPeriodSavePort rankPeriodSavePort,
        RankPeriodManagementReader rankPeriodManagementReader
    ) {
        this.rankPeriodSavePort = rankPeriodSavePort;
        this.rankPeriodManagementReader = rankPeriodManagementReader;
    }

    @Override
    public void deletePeriod(RankPeriodDeleteCommand command) {
        RankPeriodId periodId = RankPeriodId.of(command.rankPeriodId());
        RankPeriod period = rankPeriodManagementReader.findPeriodOrThrow(periodId);

        rankPeriodSavePort.delete(period);
    }
}
