package com.tastyhouse.application.rank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPrizeId;
import com.tastyhouse.application.rank.port.in.RankPrizeDeleteCommand;
import com.tastyhouse.application.rank.port.in.RankPrizeDeleteUseCase;
import com.tastyhouse.application.rank.port.out.write.RankPrizeSavePort;

@Service
@Transactional
class RankPrizeDeleteService implements RankPrizeDeleteUseCase {

    private final RankPrizeSavePort rankPrizeSavePort;
    private final RankPrizeManagementReader rankPrizeManagementReader;

    public RankPrizeDeleteService(
        RankPrizeSavePort rankPrizeSavePort,
        RankPrizeManagementReader rankPrizeManagementReader
    ) {
        this.rankPrizeSavePort = rankPrizeSavePort;
        this.rankPrizeManagementReader = rankPrizeManagementReader;
    }

    @Override
    public void deletePrize(RankPrizeDeleteCommand command) {
        RankPrizeId prizeId = RankPrizeId.of(command.rankPrizeId());
        RankPrize prize = rankPrizeManagementReader.findPrizeOrThrow(prizeId);

        rankPrizeSavePort.delete(prize);
    }
}
