package com.tastyhouse.application.rank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.model.RankPeriod;
import com.tastyhouse.application.rank.port.in.RankPeriodCreateCommand;
import com.tastyhouse.application.rank.port.in.RankPeriodCreateUseCase;
import com.tastyhouse.application.rank.port.out.write.RankPeriodPersistencePort;

@Service
@Transactional
class RankPeriodCreateService implements RankPeriodCreateUseCase {

    private final RankPeriodPersistencePort rankPeriodPersistencePort;

    public RankPeriodCreateService(RankPeriodPersistencePort rankPeriodPersistencePort) {
        this.rankPeriodPersistencePort = rankPeriodPersistencePort;
    }

    @Override
    public Long createPeriod(RankPeriodCreateCommand command) {
        RankPeriod period = RankPeriod.of(command.startAt(), command.endAt(), command.visible());
        RankPeriod saved = rankPeriodPersistencePort.save(period);
        return saved.getRankPeriodId().value();
    }
}
