package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ProductFeedback {
    private static final int CONTENT_MAX_LENGTH = 500;

    private final Long id;
    private final ProductId productId;
    private final ShopId shopId;
    private final MemberId memberId;
    private final ProductFeedbackType feedbackType;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductFeedback(
        Long id,
        ProductId productId,
        ShopId shopId,
        MemberId memberId,
        ProductFeedbackType feedbackType,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.shopId = shopId;
        this.memberId = memberId;
        this.feedbackType = feedbackType;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ProductFeedback of(
        ProductId productId,
        ShopId shopId,
        MemberId memberId,
        ProductFeedbackType feedbackType,
        String content
    ) {
        String normalizedContent = normalizeContent(content);
        validateContent(feedbackType, normalizedContent);

        return new ProductFeedback(null, productId, shopId, memberId, feedbackType, normalizedContent, null, null);
    }

    public static ProductFeedback reconstitute(
        Long id,
        ProductId productId,
        ShopId shopId,
        MemberId memberId,
        ProductFeedbackType feedbackType,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductFeedback(id, productId, shopId, memberId, feedbackType, content, createdAt, updatedAt);
    }

    private static String normalizeContent(String content) {
        if (content == null) {
            return null;
        }
        String trimmed = content.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static void validateContent(ProductFeedbackType feedbackType, String content) {
        if (feedbackType.requiresContent() && content == null) {
            throw new BusinessException(ErrorCode.PRODUCT_FEEDBACK_CONTENT_REQUIRED);
        }
        if (content != null && content.length() > CONTENT_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PRODUCT_FEEDBACK_CONTENT_TOO_LONG);
        }
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public ProductFeedbackType getFeedbackType() {
        return this.feedbackType;
    }

    public String getContent() {
        return this.content;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
