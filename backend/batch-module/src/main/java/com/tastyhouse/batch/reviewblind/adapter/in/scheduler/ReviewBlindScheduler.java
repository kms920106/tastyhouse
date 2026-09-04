package com.tastyhouse.batch.reviewblind.adapter.in.scheduler;

import com.tastyhouse.application.reviewblind.port.in.ExpireBlindedReviewsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 게시중단 만료 재노출 스케줄러.
 *
 * <p>{@code RankScheduler} 패턴 그대로 <b>스케줄러는 로깅·예외격리만</b> 담당하고 잡 본문은
 * {@link ExpireBlindedReviewsUseCase}에 둔다.
 */
@Component
public class ReviewBlindScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReviewBlindScheduler.class);

    private final ExpireBlindedReviewsUseCase expireBlindedReviewsUseCase;

    public ReviewBlindScheduler(ExpireBlindedReviewsUseCase expireBlindedReviewsUseCase) {
        this.expireBlindedReviewsUseCase = expireBlindedReviewsUseCase;
    }

    /**
     * 매일 새벽 4시 실행 — 랭킹 집계(3시)와 시간대를 분리해 두 잡이 겹치지 않게 한다.
     */
    @Scheduled(cron = "0 0 4 * * *")
    public void expireBlindedReviews() {
        log.info("=== 게시중단 만료 재노출 스케줄러 시작 ===");

        try {
            expireBlindedReviewsUseCase.expireBlindedReviews();
            log.info("=== 게시중단 만료 재노출 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("게시중단 만료 재노출 중 오류 발생", e);
        }
    }
}
