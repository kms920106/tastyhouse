package com.tastyhouse.application.reviewblind.service;

import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.reviewblind.port.in.ExpireBlindedReviewsUseCase;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.domain.review.service.ReviewBlindRequestService;

@Service
@BatchApp
public class ReviewBlindSchedulerService implements ExpireBlindedReviewsUseCase {

    private static final Logger log = LoggerFactory.getLogger(ReviewBlindSchedulerService.class);

    private final ReviewBlindRequestService reviewBlindRequestService;
    private final ReviewBlindExpirationExecutor reviewBlindExpirationExecutor;

    public ReviewBlindSchedulerService(
        ReviewBlindRequestService reviewBlindRequestService,
        ReviewBlindExpirationExecutor reviewBlindExpirationExecutor
    ) {
        this.reviewBlindRequestService = reviewBlindRequestService;
        this.reviewBlindExpirationExecutor = reviewBlindExpirationExecutor;
    }

    @Override
    public void expireBlindedReviews() {
        LocalDateTime now = LocalDateTime.now();
        List<ReviewBlindRequest> expirable = reviewBlindRequestService.findExpirableBlinds(now);

        if (expirable.isEmpty()) {
            log.info("게시중단 만료 대상 없음: now={}", now);
            return;
        }

        int succeeded = 0;
        int failed = 0;
        for (ReviewBlindRequest request : expirable) {
            if (reviewBlindExpirationExecutor.expire(request)) {
                succeeded++;
            } else {
                failed++;
            }
        }

        log.info("게시중단 만료 재노출 완료: now={}, 대상 {} 건, 성공 {} 건, 실패 {} 건",
            now, expirable.size(), succeeded, failed);
    }
}
