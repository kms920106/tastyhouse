package com.tastyhouse.application.rank.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.application.rank.port.in.RankPrizeCreateCommand;
import com.tastyhouse.application.rank.port.in.RankPrizeCreateUseCase;
import com.tastyhouse.application.rank.port.out.write.RankPrizePersistencePort;

@Service
@Transactional
class RankPrizeCreateService implements RankPrizeCreateUseCase {

    private final RankPrizePersistencePort rankPrizePersistencePort;

    public RankPrizeCreateService(RankPrizePersistencePort rankPrizePersistencePort) {
        this.rankPrizePersistencePort = rankPrizePersistencePort;
    }

    @Override
    public Long createPrize(RankPrizeCreateCommand command) {
        Long imageFileId = command.imageFileId();
        RankPeriodId rankPeriodId = RankPeriodId.of(command.rankPeriodId());
        UploadedFileId uploadedFileId = imageFileId == null ? null : UploadedFileId.of(imageFileId);

        RankPrize prize = RankPrize.of(rankPeriodId, command.prizeRank(), command.name(), command.brand(), uploadedFileId);
        RankPrize saved = rankPrizePersistencePort.save(prize);
        return saved.getRankPrizeId().value();
    }
}
