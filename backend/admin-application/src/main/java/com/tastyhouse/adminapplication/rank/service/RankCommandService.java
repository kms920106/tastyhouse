package com.tastyhouse.adminapplication.rank.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.adminapplication.rank.port.in.RankAggregateCommand;
import com.tastyhouse.adminapplication.rank.port.in.RankCommandUseCase;
import com.tastyhouse.adminapplication.rank.port.in.RankPeriodCreateCommand;
import com.tastyhouse.adminapplication.rank.port.in.RankPeriodDeleteCommand;
import com.tastyhouse.adminapplication.rank.port.in.RankPeriodUpdateCommand;
import com.tastyhouse.adminapplication.rank.port.in.RankPrizeCreateCommand;
import com.tastyhouse.adminapplication.rank.port.in.RankPrizeDeleteCommand;
import com.tastyhouse.adminapplication.rank.port.in.RankPrizeUpdateCommand;
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

/**
 * 랭킹 관리 명령 서비스(admin).
 *
 * <p>랭킹 기간·경품 CRUD는 각각 단일 애그리거트({@code RankPeriod} / {@code RankPrize}) 조작이므로
 * write 포트를 직접 주입해 이 서비스가 처리한다(분류 A). 반면 랭킹 수동 재집계는 기존 랭킹 삭제와 신규
 * 순위 적재가 함께 일어나야 하는 다중 애그리거트 원자 연산이므로 도메인 서비스
 * ({@link RankSettlementService})에 위임한다 — batch의 야간 자동 집계와 같은 규칙을 공유한다.
 *
 * <p>두 도메인 모델은 순수 POJO라 더티 체킹이 없으므로 변경 후 명시적으로 {@code save}를 호출한다.
 * HTTP 경계에서 받은 {@code Long}·{@code String}은 이 계층에서 {@code RankPeriodId}·{@code RankPrizeId}·
 * {@code RankType}으로 승격한다. 삭제는 소프트 삭제이며 어댑터의 {@code delete}가 플래그만 갱신한다.
 */
@Service
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

    /**
     * 랭킹을 수동으로 재집계한다. 타입 미지정이면 전체·월간·주간을 모두, 지정하면 해당 타입만 집계한다.
     */
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
