package com.tastyhouse.application.reviewblind.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.review.model.ReviewBlindRequest;
import com.tastyhouse.application.review.service.ReviewBlindRequestService;

@Component
public class ReviewBlindExpirationExecutor {

    private static final Logger log = LoggerFactory.getLogger(ReviewBlindExpirationExecutor.class);

    private final ReviewBlindRequestService reviewBlindRequestService;

    public ReviewBlindExpirationExecutor(ReviewBlindRequestService reviewBlindRequestService) {
        this.reviewBlindRequestService = reviewBlindRequestService;
    }

    @Transactional
    public boolean expire(ReviewBlindRequest request) {
        try {
            reviewBlindRequestService.expire(request.getId());
            return true;
        } catch (Exception e) {
            log.error("게시중단 만료 재노출 실패: blindRequestId={}, reviewId={}, blindUntil={}",
                request.getId(), request.getReviewId().value(), request.getBlindUntil(), e);
            return false;
        }
    }
}
