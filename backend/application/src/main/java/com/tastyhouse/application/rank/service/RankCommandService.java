package com.tastyhouse.application.rank.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.rank.port.in.RankAggregateCommand;
import com.tastyhouse.application.rank.port.in.RankCommandUseCase;
import com.tastyhouse.application.rank.port.in.RankPeriodCreateCommand;
import com.tastyhouse.application.rank.port.in.RankPeriodDeleteCommand;
import com.tastyhouse.application.rank.port.in.RankPeriodUpdateCommand;
import com.tastyhouse.application.rank.port.in.RankPrizeCreateCommand;
import com.tastyhouse.application.rank.port.in.RankPrizeDeleteCommand;
import com.tastyhouse.application.rank.port.in.RankPrizeUpdateCommand;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.rank.model.RankPeriod;
import com.tastyhouse.domain.rank.model.RankPrize;
import com.tastyhouse.domain.rank.model.RankType;
import com.tastyhouse.domain.rank.repository.RankPeriodRepository;
import com.tastyhouse.domain.rank.repository.RankPrizeRepository;
import com.tastyhouse.domain.rank.service.RankSettlementService;
import com.tastyhouse.domain.rank.vo.RankPeriodId;
import com.tastyhouse.domain.rank.vo.RankPrizeId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class RankCommandService implements RankCommandUseCase {

    private static final int DEFAULT_AGGREGATE_LIMIT = 10;

    private final RankPeriodRepository rankPeriodRepository;
    private final RankPrizeRepository rankPrizeRepository;
    private final RankSettlementService rankSettlementService;

    public RankCommandService(
        RankPeriodRepository rankPeriodRepository,
        RankPrizeRepository rankPrizeRepository,
        RankSettlementService rankSettlementService
    ) {
        this.rankPeriodRepository = rankPeriodRepository;
        this.rankPrizeRepository = rankPrizeRepository;
        this.rankSettlementService = rankSettlementService;
    }

    @Override
    public void aggregate(RankAggregateCommand command) {
        String type = command.type();
        if (type == null) {
            rankSettlementService.settleAll(LocalDate.now());
            return;
        }

        LocalDate baseDate = command.baseDate();
        Integer limit = command.limit();
        RankType rankType = RankType.from(type);
        LocalDate targetDate = baseDate != null ? baseDate : LocalDate.now();
        int targetLimit = limit != null ? limit : DEFAULT_AGGREGATE_LIMIT;
        rankSettlementService.settle(rankType, targetDate, targetLimit);
    }

    @Override
    public Long createPeriod(RankPeriodCreateCommand command) {
        RankPeriod period = RankPeriod.of(command.startAt(), command.endAt(), command.visible());
        RankPeriod saved = rankPeriodRepository.save(period);
        return saved.getRankPeriodId().value();
    }

    @Override
    public void updatePeriod(RankPeriodUpdateCommand command) {
        RankPeriodId periodId = RankPeriodId.of(command.rankPeriodId());
        RankPeriod period = findPeriodOrThrow(periodId);

        period.update(command.startAt(), command.endAt(), command.visible());
        rankPeriodRepository.save(period);
    }

    @Override
    public void deletePeriod(RankPeriodDeleteCommand command) {
        RankPeriodId periodId = RankPeriodId.of(command.rankPeriodId());
        RankPeriod period = findPeriodOrThrow(periodId);

        rankPeriodRepository.delete(period);
    }

    @Override
    public Long createPrize(RankPrizeCreateCommand command) {
        Long imageFileId = command.imageFileId();
        RankPeriodId rankPeriodId = RankPeriodId.of(command.rankPeriodId());
        UploadedFileId uploadedFileId = imageFileId == null ? null : UploadedFileId.of(imageFileId);

        RankPrize prize = RankPrize.of(rankPeriodId, command.prizeRank(), command.name(), command.brand(), uploadedFileId);
        RankPrize saved = rankPrizeRepository.save(prize);
        return saved.getRankPrizeId().value();
    }

    @Override
    public void updatePrize(RankPrizeUpdateCommand command) {
        Long imageFileId = command.imageFileId();
        RankPrizeId prizeId = RankPrizeId.of(command.rankPrizeId());
        RankPrize prize = findPrizeOrThrow(prizeId);
        UploadedFileId uploadedFileId = imageFileId == null ? null : UploadedFileId.of(imageFileId);

        prize.update(command.prizeRank(), command.name(), command.brand(), uploadedFileId);
        rankPrizeRepository.save(prize);
    }

    @Override
    public void deletePrize(RankPrizeDeleteCommand command) {
        RankPrizeId prizeId = RankPrizeId.of(command.rankPrizeId());
        RankPrize prize = findPrizeOrThrow(prizeId);

        rankPrizeRepository.delete(prize);
    }

    private RankPeriod findPeriodOrThrow(RankPeriodId periodId) {
        return rankPeriodRepository.findById(periodId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RANK_PERIOD_NOT_FOUND));
    }

    private RankPrize findPrizeOrThrow(RankPrizeId prizeId) {
        return rankPrizeRepository.findById(prizeId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RANK_PRIZE_NOT_FOUND));
    }
}
