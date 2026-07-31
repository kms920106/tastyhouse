package com.tastyhouse.adminapi.rank;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.domain.model.RankPeriod;
import com.tastyhouse.domain.rank.domain.model.RankPrize;
import com.tastyhouse.domain.rank.domain.model.RankType;
import com.tastyhouse.domain.rank.domain.repository.RankPeriodRepository;
import com.tastyhouse.domain.rank.domain.repository.RankPrizeRepository;
import com.tastyhouse.domain.rank.domain.service.RankSettlementService;
import com.tastyhouse.domain.rank.domain.vo.RankPeriodId;
import com.tastyhouse.domain.rank.domain.vo.RankPrizeId;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;

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
@RequiredArgsConstructor
public class RankCommandService {

    private static final int DEFAULT_AGGREGATE_LIMIT = 10;

    private final RankPeriodRepository rankPeriodRepository;
    private final RankPrizeRepository rankPrizeRepository;
    private final RankSettlementService rankSettlementService;

    /**
     * 랭킹을 수동으로 재집계한다. 타입 미지정이면 전체·월간·주간을 모두, 지정하면 해당 타입만 집계한다.
     */
    public void aggregate(String type, LocalDate baseDate, Integer limit) {
        if (type == null) {
            rankSettlementService.settleAll(LocalDate.now());
            return;
        }

        RankType rankType = RankType.from(type);
        LocalDate targetDate = baseDate != null ? baseDate : LocalDate.now();
        int targetLimit = limit != null ? limit : DEFAULT_AGGREGATE_LIMIT;
        rankSettlementService.settle(rankType, targetDate, targetLimit);
    }

    public Long createPeriod(LocalDateTime startAt, LocalDateTime endAt, boolean visible) {
        RankPeriod period = RankPeriod.of(startAt, endAt, visible);
        RankPeriod saved = rankPeriodRepository.save(period);
        return saved.getRankPeriodId().value();
    }

    public void updatePeriod(Long id, LocalDateTime startAt, LocalDateTime endAt, boolean visible) {
        RankPeriodId periodId = RankPeriodId.of(id);
        RankPeriod period = findPeriodOrThrow(periodId);

        period.update(startAt, endAt, visible);
        rankPeriodRepository.save(period);
    }

    public void deletePeriod(Long id) {
        RankPeriodId periodId = RankPeriodId.of(id);
        RankPeriod period = findPeriodOrThrow(periodId);

        rankPeriodRepository.delete(period);
    }

    public Long createPrize(Long periodId, Integer prizeRank, String name, String brand, Long imageFileId) {
        RankPeriodId rankPeriodId = RankPeriodId.of(periodId);

        RankPrize prize = RankPrize.of(rankPeriodId.value(), prizeRank, name, brand, imageFileId);
        RankPrize saved = rankPrizeRepository.save(prize);
        return saved.getRankPrizeId().value();
    }

    public void updatePrize(Long id, Integer prizeRank, String name, String brand, Long imageFileId) {
        RankPrizeId prizeId = RankPrizeId.of(id);
        RankPrize prize = findPrizeOrThrow(prizeId);

        prize.update(prizeRank, name, brand, imageFileId);
        rankPrizeRepository.save(prize);
    }

    public void deletePrize(Long id) {
        RankPrizeId prizeId = RankPrizeId.of(id);
        RankPrize prize = findPrizeOrThrow(prizeId);

        rankPrizeRepository.delete(prize);
    }

    private RankPeriod findPeriodOrThrow(RankPeriodId periodId) {
        return rankPeriodRepository.findById(periodId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RANK_PERIOD_NOT_FOUND));
    }

    private RankPrize findPrizeOrThrow(RankPrizeId prizeId) {
        return rankPrizeRepository.findById(prizeId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.RANK_PRIZE_NOT_FOUND));
    }
}
