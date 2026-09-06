package com.tastyhouse.domain.product.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.model.ProductFeedback;
import com.tastyhouse.domain.product.model.ProductFeedbackRead;
import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.domain.product.repository.ProductFeedbackReadRepository;
import com.tastyhouse.domain.product.repository.ProductFeedbackRepository;
import com.tastyhouse.domain.product.repository.ProductRepository;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductFeedbackService {
    public static final int FEEDBACK_WINDOW_DAYS = 7;

    private final ProductRepository productRepository;
    private final ProductFeedbackRepository productFeedbackRepository;
    private final ProductFeedbackReadRepository productFeedbackReadRepository;

    public ProductFeedbackService(
        ProductRepository productRepository,
        ProductFeedbackRepository productFeedbackRepository,
        ProductFeedbackReadRepository productFeedbackReadRepository
    ) {
        this.productRepository = productRepository;
        this.productFeedbackRepository = productFeedbackRepository;
        this.productFeedbackReadRepository = productFeedbackReadRepository;
    }

    public ProductFeedback submit(
        MemberId memberId,
        ProductId productId,
        ProductFeedbackType feedbackType,
        String content,
        LocalDateTime now
    ) {
        Product product = productRepository.findById(productId)
            .filter(found -> !found.isDeleted())
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND));

        LocalDateTime windowStart = now.minusDays(FEEDBACK_WINDOW_DAYS);
        if (productFeedbackRepository.existsRecentDuplicate(memberId, productId, feedbackType, windowStart)) {
            throw new BusinessException(ErrorCode.PRODUCT_FEEDBACK_ALREADY_SUBMITTED);
        }

        ProductFeedback feedback = ProductFeedback.of(
            productId, product.getShopId(), memberId, feedbackType, content
        );
        return productFeedbackRepository.save(feedback);
    }

    public boolean hasUnread(ShopId shopId, LocalDateTime now) {
        LocalDateTime windowStart = now.minusDays(FEEDBACK_WINDOW_DAYS);
        LocalDateTime since = productFeedbackReadRepository.findByShopId(shopId)
            .map(ProductFeedbackRead::getReadAt)

            .filter(readAt -> readAt.isAfter(windowStart))
            .orElse(windowStart);

        return productFeedbackRepository.existsByShopIdAndCreatedAtAfter(shopId, since);
    }

    public void markRead(ShopId shopId, LocalDateTime now) {
        ProductFeedbackRead feedbackRead = productFeedbackReadRepository.findByShopId(shopId)
            .orElseGet(() -> ProductFeedbackRead.of(shopId, now));
        feedbackRead.markRead(now);
        productFeedbackReadRepository.save(feedbackRead);
    }
}
