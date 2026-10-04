package com.tastyhouse.application.product.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.model.ProductFeedbackRead;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackReadPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class ProductFeedbackService {

    public static final int FEEDBACK_WINDOW_DAYS = 7;

    private final ProductPersistencePort productPersistencePort;
    private final ProductFeedbackPersistencePort productFeedbackPersistencePort;
    private final ProductFeedbackReadPersistencePort productFeedbackReadPersistencePort;

    public ProductFeedbackService(
        ProductPersistencePort productPersistencePort,
        ProductFeedbackPersistencePort productFeedbackPersistencePort,
        ProductFeedbackReadPersistencePort productFeedbackReadPersistencePort
    ) {
        this.productPersistencePort = productPersistencePort;
        this.productFeedbackPersistencePort = productFeedbackPersistencePort;
        this.productFeedbackReadPersistencePort = productFeedbackReadPersistencePort;
    }

    public ProductFeedback submit(
        MemberId memberId,
        ProductId productId,
        ProductFeedbackType feedbackType,
        String content,
        LocalDateTime now
    ) {
        Product product = productPersistencePort.findById(productId)
            .filter(found -> !found.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));

        LocalDateTime windowStart = now.minusDays(FEEDBACK_WINDOW_DAYS);
        if (productFeedbackPersistencePort.existsRecentDuplicate(memberId, productId, feedbackType, windowStart)) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_FEEDBACK_ALREADY_SUBMITTED);
        }

        ProductFeedback feedback = ProductFeedback.of(
            productId, product.getShopId(), memberId, feedbackType, content
        );
        return productFeedbackPersistencePort.save(feedback);
    }

    public boolean hasUnread(ShopId shopId, LocalDateTime now) {
        LocalDateTime windowStart = now.minusDays(FEEDBACK_WINDOW_DAYS);
        LocalDateTime since = productFeedbackReadPersistencePort.findByShopId(shopId)
            .map(ProductFeedbackRead::getReadAt)

            .filter(readAt -> readAt.isAfter(windowStart))
            .orElse(windowStart);

        return productFeedbackPersistencePort.existsByShopIdAndCreatedAtAfter(shopId, since);
    }

    public void markRead(ShopId shopId, LocalDateTime now) {
        ProductFeedbackRead feedbackRead = productFeedbackReadPersistencePort.findByShopId(shopId)
            .orElseGet(() -> ProductFeedbackRead.of(shopId, now));
        feedbackRead.markRead(now);
        productFeedbackReadPersistencePort.save(feedbackRead);
    }
}
