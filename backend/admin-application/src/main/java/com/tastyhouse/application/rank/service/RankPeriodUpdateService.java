package com.tastyhouse.application.rank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.model.RankPeriod;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.application.rank.port.in.RankPeriodUpdateCommand;
import com.tastyhouse.application.rank.port.in.RankPeriodUpdateUseCase;
import com.tastyhouse.application.rank.port.out.write.RankPeriodPersistencePort;

@Service
@Transactional
class RankPeriodUpdateService implements RankPeriodUpdateUseCase {

    private final RankPeriodPersistencePort rankPeriodPersistencePort;
    private final RankPeriodManagementReader rankPeriodManagementReader;

    public RankPeriodUpdateService(
        RankPeriodPersistencePort rankPeriodPersistencePort,
        RankPeriodManagementReader rankPeriodManagementReader
    ) {
        this.rankPeriodPersistencePort = rankPeriodPersistencePort;
        this.rankPeriodManagementReader = rankPeriodManagementReader;
    }

    @Override
    public void updatePeriod(RankPeriodUpdateCommand command) {
        RankPeriodId periodId = RankPeriodId.of(command.rankPeriodId());
        RankPeriod period = rankPeriodManagementReader.findPeriodOrThrow(periodId);

        period.update(command.startAt(), command.endAt(), command.visible());
        rankPeriodPersistencePort.save(period);
    }
}
