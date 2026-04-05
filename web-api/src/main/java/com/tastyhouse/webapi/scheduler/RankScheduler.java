package com.tastyhouse.webapi.scheduler;

import com.tastyhouse.core.service.RankAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RankScheduler {

    private final RankAggregationService rankAggregationService;

//    @Scheduled(cron = "0 * * * * *") // 1분마다 실행 (테스트용)
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시 실행 (운영용)
    public void aggregateRanks() {
        log.info("=== 랭킹 집계 스케줄러 시작 ===");

        try {
            rankAggregationService.aggregateAllRanks();
            log.info("=== 랭킹 집계 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("랭킹 집계 중 오류 발생", e);
        }
    }
}
