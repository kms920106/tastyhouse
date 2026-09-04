package com.tastyhouse.batch.grade.adapter.in.scheduler;

import com.tastyhouse.application.grade.port.in.SettleMemberGradesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class GradeScheduler {

    private static final Logger log = LoggerFactory.getLogger(GradeScheduler.class);

    private final SettleMemberGradesUseCase settleMemberGradesUseCase;

    public GradeScheduler(SettleMemberGradesUseCase settleMemberGradesUseCase) {
        this.settleMemberGradesUseCase = settleMemberGradesUseCase;
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
            settleMemberGradesUseCase.updateAllMemberGrades();
            log.info("=== 회원 등급 업데이트 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("회원 등급 업데이트 중 오류 발생", e);
        }
    }
}
