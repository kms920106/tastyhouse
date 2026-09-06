package com.tastyhouse.infrastructure.notification.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.notification.service.NotificationService;
import com.tastyhouse.domain.review.event.ReviewBlindApprovedEvent;

@Component
public class ReviewBlindApprovedEventListener {
    private static final Logger log = LoggerFactory.getLogger(ReviewBlindApprovedEventListener.class);

    private final NotificationService notificationService;

    public ReviewBlindApprovedEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReviewBlindApprovedEvent event) {
        Long notificationId = notificationService.notifyReviewBlindApproved(
            event.reviewerMemberId(),
            event.reviewId(),
            event.blindUntil()
        );

        log.info("게시중단 승인 알림 적재 완료 — notificationId={}, reviewId={}, memberId={}, blindRequestId={}, blindUntil={}",
            notificationId,
            event.reviewId().value(),
            event.reviewerMemberId().value(),
            event.blindRequestId().value(),
            event.blindUntil()
        );
    }
}
