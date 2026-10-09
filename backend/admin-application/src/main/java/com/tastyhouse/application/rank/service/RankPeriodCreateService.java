package com.tastyhouse.application.rank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.model.RankPeriod;
import com.tastyhouse.application.rank.port.in.RankPeriodCreateCommand;
import com.tastyhouse.application.rank.port.in.RankPeriodCreateUseCase;
import com.tastyhouse.application.rank.port.out.write.RankPeriodSavePort;

@Service
@Transactional
class RankPeriodCreateService implements RankPeriodCreateUseCase {

    private final RankPeriodSavePort rankPeriodSavePort;

    public RankPeriodCreateService(RankPeriodSavePort rankPeriodSavePort) {
        this.rankPeriodSavePort = rankPeriodSavePort;
    }

    @Override
    public Long createPeriod(RankPeriodCreateCommand command) {
        RankPeriod period = RankPeriod.of(command.startAt(), command.endAt(), command.visible());
        RankPeriod saved = rankPeriodSavePort.save(period);
        return saved.getRankPeriodId().value();
    }
}
