package com.tastyhouse.application.rank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPrizeId;
import com.tastyhouse.application.rank.port.in.RankPrizeDeleteCommand;
import com.tastyhouse.application.rank.port.in.RankPrizeDeleteUseCase;
import com.tastyhouse.application.rank.port.out.write.RankPrizePersistencePort;

@Service
@Transactional
class RankPrizeDeleteService implements RankPrizeDeleteUseCase {

    private final RankPrizePersistencePort rankPrizePersistencePort;
    private final RankPrizeManagementReader rankPrizeManagementReader;

    public RankPrizeDeleteService(
        RankPrizePersistencePort rankPrizePersistencePort,
        RankPrizeManagementReader rankPrizeManagementReader
    ) {
        this.rankPrizePersistencePort = rankPrizePersistencePort;
        this.rankPrizeManagementReader = rankPrizeManagementReader;
    }

    @Override
    public void deletePrize(RankPrizeDeleteCommand command) {
        RankPrizeId prizeId = RankPrizeId.of(command.rankPrizeId());
        RankPrize prize = rankPrizeManagementReader.findPrizeOrThrow(prizeId);

        rankPrizePersistencePort.delete(prize);
    }
}
