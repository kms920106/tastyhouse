package com.tastyhouse.core.domain.product.application;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.core.domain.product.domain.port.ProductReviewStatisticsPort;
import com.tastyhouse.core.domain.product.domain.repository.ProductRepository;
import com.tastyhouse.core.domain.product.domain.vo.ProductId;
import com.tastyhouse.core.domain.review.domain.event.ReviewCreatedEvent;
import com.tastyhouse.core.domain.review.domain.event.ReviewDeletedEvent;

@Component
@RequiredArgsConstructor
public class ProductReviewEventListener {

    private final ProductRepository productRepository;
    private final ProductReviewStatisticsPort productReviewStatisticsPort;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewCreated(ReviewCreatedEvent event) {
        if (event.productId() == null) return;
        updateProductReviewStats(event.productId());
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReviewDeleted(ReviewDeletedEvent event) {
        if (event.productId() == null) return;
        updateProductReviewStats(event.productId());
    }

    private void updateProductReviewStats(Long productId) {
        productRepository.findById(ProductId.of(productId)).ifPresent(product -> {
            Long count = productReviewStatisticsPort.countVisibleReviewsByProductId(productId);
            Double rating = calculateAverageRating(productId);
            product.updateReviewStats(rating, count != null ? count.intValue() : 0);
            productRepository.save(product);
        });
    }

    private Double calculateAverageRating(Long productId) {
        Double taste = productReviewStatisticsPort.getAverageTasteRatingByProductId(productId);
        Double amount = productReviewStatisticsPort.getAverageAmountRatingByProductId(productId);
        Double price = productReviewStatisticsPort.getAveragePriceRatingByProductId(productId);
        if (taste == null && amount == null && price == null) return null;
        double t = taste != null ? taste : 0.0;
        double a = amount != null ? amount : 0.0;
        double p = price != null ? price : 0.0;
        int divisor = (taste != null ? 1 : 0) + (amount != null ? 1 : 0) + (price != null ? 1 : 0);
        return Math.round((t + a + p) / divisor * 10.0) / 10.0;
    }
}
