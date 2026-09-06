package com.tastyhouse.infrastructure.product.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.menureview.event.MenuReviewCreatedEvent;
import com.tastyhouse.domain.menureview.event.MenuReviewDeletedEvent;
import com.tastyhouse.domain.menureview.event.MenuReviewRatingChangedEvent;
import com.tastyhouse.domain.product.service.ProductReviewStatsService;

@Component
public class ProductMenuReviewEventListener {
    private final ProductReviewStatsService productReviewStatsService;

    public ProductMenuReviewEventListener(ProductReviewStatsService productReviewStatsService) {
        this.productReviewStatsService = productReviewStatsService;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMenuReviewCreated(MenuReviewCreatedEvent event) {
        if (event.productId() == null) {
            return;
        }
        productReviewStatsService.updateReviewStats(event.productId().value());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMenuReviewRatingChanged(MenuReviewRatingChangedEvent event) {
        if (event.productId() == null) {
            return;
        }
        productReviewStatsService.updateReviewStats(event.productId().value());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMenuReviewDeleted(MenuReviewDeletedEvent event) {
        if (event.productId() == null) {
            return;
        }
        productReviewStatsService.updateReviewStats(event.productId().value());
    }
}
