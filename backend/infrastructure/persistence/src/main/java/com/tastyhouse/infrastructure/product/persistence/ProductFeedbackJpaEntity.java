package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.product.model.ProductFeedbackType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_FEEDBACK")
public class ProductFeedbackJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "feedback_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ProductFeedbackType feedbackType;

    @Column(name = "content", length = 500)
    private String content;

    protected ProductFeedbackJpaEntity() {
    }

    private ProductFeedbackJpaEntity(
        Long productId,
        Long shopId,
        Long memberId,
        ProductFeedbackType feedbackType,
        String content
    ) {
        this.productId = productId;
        this.shopId = shopId;
        this.memberId = memberId;
        this.feedbackType = feedbackType;
        this.content = content;
    }

    static ProductFeedbackJpaEntity create(
        Long productId,
        Long shopId,
        Long memberId,
        ProductFeedbackType feedbackType,
        String content
    ) {
        return new ProductFeedbackJpaEntity(productId, shopId, memberId, feedbackType, content);
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public ProductFeedbackType getFeedbackType() {
        return this.feedbackType;
    }

    public String getContent() {
        return this.content;
    }
}
