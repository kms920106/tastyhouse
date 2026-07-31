package com.tastyhouse.batch.scheduler;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.rank.domain.service.RankSettlementService;

/**
 * 랭킹 집계 배치 서비스.
 *
 * <p>랭킹 확정 규칙 자체는 도메인 서비스({@link RankSettlementService})가 갖고 있고, 이 서비스는 배치
 * 트리거의 트랜잭션 경계만 선언한다(공통 지침 패턴 1 — 도메인 서비스는 {@code @Transactional}을 갖지
 * 않는다). 관리자의 수동 재집계(admin-api)도 같은 도메인 서비스를 호출하므로 집계 규칙이 갈리지 않는다.
 *
 * <p>타입별 집계는 각각 독립 트랜잭션이 아니라 한 트랜잭션에서 함께 처리한다 — 기존 동작
 * ({@code aggregateAllRanks}가 단일 {@code @Transactional} 안에서 세 타입을 순차 집계)을 보존한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankSchedulerService {

    private final RankSettlementService rankSettlementService;

    /**
     * 오늘 기준으로 전체·월간·주간 랭킹을 모두 재집계한다.
     */
    @Transactional
    public void aggregateAllRanks() {
        LocalDate baseDate = LocalDate.now();

        int settled = rankSettlementService.settleAll(baseDate);

        log.info("랭킹 집계 완료: baseDate={}, 적재 {} 건", baseDate, settled);
    }
}
