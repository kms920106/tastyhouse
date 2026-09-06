package com.tastyhouse.batch.reviewblind.adapter.in.scheduler;

import com.tastyhouse.application.reviewblind.port.in.ExpireBlindedReviewsUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReviewBlindScheduler {

    private static final Logger log = LoggerFactory.getLogger(ReviewBlindScheduler.class);

    private final ExpireBlindedReviewsUseCase expireBlindedReviewsUseCase;

    public ReviewBlindScheduler(ExpireBlindedReviewsUseCase expireBlindedReviewsUseCase) {
        this.expireBlindedReviewsUseCase = expireBlindedReviewsUseCase;
    }

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
