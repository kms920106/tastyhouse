package com.tastyhouse.webapi.scheduler;

import com.tastyhouse.core.service.MemberGradeService;
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
    private final MemberGradeService memberGradeService;

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

    /**
     * 회원 등급 업데이트 스케줄러
     * - 매일 새벽 3시 10분에 실행 (랭킹 집계 이후)
     * - 리뷰 개수 기준으로 회원 등급 업데이트
     */
//    @Scheduled(cron = "0 * * * * *") // 1분마다 실행 (테스트용)
    @Scheduled(cron = "0 30 3 * * *") // 매일 새벽 3시 30분 실행 (운영용)
    public void updateMemberGrades() {
        log.info("=== 회원 등급 업데이트 스케줄러 시작 ===");

        try {
            memberGradeService.updateAllMemberGrades();
            log.info("=== 회원 등급 업데이트 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("회원 등급 업데이트 중 오류 발생", e);
        }
    }
}
