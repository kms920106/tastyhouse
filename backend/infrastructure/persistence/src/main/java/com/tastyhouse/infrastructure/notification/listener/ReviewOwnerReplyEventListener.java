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
import com.tastyhouse.domain.review.event.ReviewOwnerReplyCreatedEvent;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;

@Component
public class ReviewOwnerReplyEventListener {
    private static final Logger log = LoggerFactory.getLogger(ReviewOwnerReplyEventListener.class);

    private final NotificationService notificationService;
    private final ShopQueryDao shopQueryDao;

    public ReviewOwnerReplyEventListener(NotificationService notificationService, ShopQueryDao shopQueryDao) {
        this.notificationService = notificationService;
        this.shopQueryDao = shopQueryDao;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ReviewOwnerReplyCreatedEvent event) {
        String shopName = shopQueryDao.findShopName(event.shopId().value()).orElse(null);

        Long notificationId = notificationService.notifyReviewOwnerReply(
            event.reviewerMemberId(),
            event.reviewId(),
            shopName
        );

        log.info("사장님 답변 알림 적재 완료 — notificationId={}, reviewId={}, memberId={}, shopId={}, ownerReplyId={}",
            notificationId,
            event.reviewId().value(),
            event.reviewerMemberId().value(),
            event.shopId().value(),
            event.ownerReplyId().value()
        );
    }
}
