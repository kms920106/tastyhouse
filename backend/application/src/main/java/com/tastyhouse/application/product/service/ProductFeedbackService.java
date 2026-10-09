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
import com.tastyhouse.application.product.port.out.write.ProductFeedbackLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackReadLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackReadSavePort;
import com.tastyhouse.application.product.port.out.write.ProductFeedbackSavePort;
import com.tastyhouse.application.product.port.out.write.ProductLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
public class ProductFeedbackService {

    public static final int FEEDBACK_WINDOW_DAYS = 7;

    private final ProductLoadPort productLoadPort;
    private final ProductFeedbackLoadPort productFeedbackLoadPort;
    private final ProductFeedbackSavePort productFeedbackSavePort;
    private final ProductFeedbackReadLoadPort productFeedbackReadLoadPort;
    private final ProductFeedbackReadSavePort productFeedbackReadSavePort;

    public ProductFeedbackService(
        ProductLoadPort productLoadPort,
        ProductFeedbackLoadPort productFeedbackLoadPort,
        ProductFeedbackSavePort productFeedbackSavePort,
        ProductFeedbackReadLoadPort productFeedbackReadLoadPort,
        ProductFeedbackReadSavePort productFeedbackReadSavePort
    ) {
        this.productLoadPort = productLoadPort;
        this.productFeedbackLoadPort = productFeedbackLoadPort;
        this.productFeedbackSavePort = productFeedbackSavePort;
        this.productFeedbackReadLoadPort = productFeedbackReadLoadPort;
        this.productFeedbackReadSavePort = productFeedbackReadSavePort;
    }

    public ProductFeedback submit(
        MemberId memberId,
        ProductId productId,
        ProductFeedbackType feedbackType,
        String content,
        LocalDateTime now
    ) {
        Product product = productLoadPort.findById(productId)
            .filter(found -> !found.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));

        LocalDateTime windowStart = now.minusDays(FEEDBACK_WINDOW_DAYS);
        if (productFeedbackLoadPort.existsRecentDuplicate(memberId, productId, feedbackType, windowStart)) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_FEEDBACK_ALREADY_SUBMITTED);
        }

        ProductFeedback feedback = ProductFeedback.of(
            productId, product.getShopId(), memberId, feedbackType, content
        );
        return productFeedbackSavePort.save(feedback);
    }

    public boolean hasUnread(ShopId shopId, LocalDateTime now) {
        LocalDateTime windowStart = now.minusDays(FEEDBACK_WINDOW_DAYS);
        LocalDateTime since = productFeedbackReadLoadPort.findByShopId(shopId)
            .map(ProductFeedbackRead::getReadAt)

            .filter(readAt -> readAt.isAfter(windowStart))
            .orElse(windowStart);

        return productFeedbackLoadPort.existsByShopIdAndCreatedAtAfter(shopId, since);
    }

    public void markRead(ShopId shopId, LocalDateTime now) {
        ProductFeedbackRead feedbackRead = productFeedbackReadLoadPort.findByShopId(shopId)
            .orElseGet(() -> ProductFeedbackRead.of(shopId, now));
        feedbackRead.markRead(now);
        productFeedbackReadSavePort.save(feedbackRead);
    }
}
