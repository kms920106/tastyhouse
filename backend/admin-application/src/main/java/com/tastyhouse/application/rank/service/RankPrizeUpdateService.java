package com.tastyhouse.application.rank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPrizeId;
import com.tastyhouse.application.rank.port.in.RankPrizeUpdateCommand;
import com.tastyhouse.application.rank.port.in.RankPrizeUpdateUseCase;
import com.tastyhouse.application.rank.port.out.write.RankPrizeSavePort;

@Service
@Transactional
class RankPrizeUpdateService implements RankPrizeUpdateUseCase {

    private final RankPrizeSavePort rankPrizeSavePort;
    private final RankPrizeManagementReader rankPrizeManagementReader;

    public RankPrizeUpdateService(
        RankPrizeSavePort rankPrizeSavePort,
        RankPrizeManagementReader rankPrizeManagementReader
    ) {
        this.rankPrizeSavePort = rankPrizeSavePort;
        this.rankPrizeManagementReader = rankPrizeManagementReader;
    }

    @Override
    public void updatePrize(RankPrizeUpdateCommand command) {
        Long imageFileId = command.imageFileId();
        RankPrizeId prizeId = RankPrizeId.of(command.rankPrizeId());
        RankPrize prize = rankPrizeManagementReader.findPrizeOrThrow(prizeId);
        UploadedFileId uploadedFileId = imageFileId == null ? null : UploadedFileId.of(imageFileId);

        prize.update(command.prizeRank(), command.name(), command.brand(), uploadedFileId);
        rankPrizeSavePort.save(prize);
    }
}
