package com.tastyhouse.batch.rank.adapter.in.scheduler;

import com.tastyhouse.application.rank.port.in.AggregateRanksUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RankScheduler {

    private static final Logger log = LoggerFactory.getLogger(RankScheduler.class);

    private final AggregateRanksUseCase aggregateRanksUseCase;

    public RankScheduler(AggregateRanksUseCase aggregateRanksUseCase) {
        this.aggregateRanksUseCase = aggregateRanksUseCase;
    }

//    @Scheduled(cron = "0 * * * * *") // 1분마다 실행 (테스트용)
    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시 실행 (운영용)
    public void aggregateRanks() {
        log.info("=== 랭킹 집계 스케줄러 시작 ===");

        try {
            aggregateRanksUseCase.aggregateAllRanks();
            log.info("=== 랭킹 집계 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("랭킹 집계 중 오류 발생", e);
        }
    }
}
