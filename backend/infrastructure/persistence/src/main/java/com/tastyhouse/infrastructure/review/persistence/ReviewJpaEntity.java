package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "REVIEW")
public class ReviewJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "total_rating", nullable = false)
    private Double totalRating;

    @Column(name = "taste_rating")
    private Double tasteRating;

    @Column(name = "amount_rating")
    private Double amountRating;

    @Column(name = "price_rating")
    private Double priceRating;

    @Column(name = "atmosphere_rating")
    private Double atmosphereRating;

    @Column(name = "kindness_rating")
    private Double kindnessRating;

    @Column(name = "hygiene_rating")
    private Double hygieneRating;

    @Column(name = "will_revisit", nullable = false)
    private boolean willRevisit;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    @Column(name = "is_owner_only", nullable = false)
    private boolean ownerOnly;

    @Column(name = "delivery_rating")
    private Integer deliveryRating;

    @Column(name = "delivery_comment", length = 500)
    private String deliveryComment;

    protected ReviewJpaEntity() {
    }

    private ReviewJpaEntity(
        Long shopId,
        Long productId,
        Long memberId,
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
        Long orderId,
        boolean hidden,
        boolean ownerOnly,
        Integer deliveryRating,
        String deliveryComment
    ) {
        this.shopId = shopId;
        this.productId = productId;
        this.memberId = memberId;
        this.content = content;
        this.totalRating = totalRating;
        this.tasteRating = tasteRating;
        this.amountRating = amountRating;
        this.priceRating = priceRating;
        this.atmosphereRating = atmosphereRating;
        this.kindnessRating = kindnessRating;
        this.hygieneRating = hygieneRating;
        this.willRevisit = willRevisit;
        this.orderId = orderId;
        this.hidden = hidden;
        this.ownerOnly = ownerOnly;
        this.deliveryRating = deliveryRating;
        this.deliveryComment = deliveryComment;
    }

    static ReviewJpaEntity create(
        Long shopId,
        Long productId,
        Long memberId,
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
        Long orderId,
        boolean hidden,
        boolean ownerOnly,
        Integer deliveryRating,
        String deliveryComment
    ) {
        return new ReviewJpaEntity(
            shopId,
            productId,
            memberId,
            content,
            totalRating,
            tasteRating,
            amountRating,
            priceRating,
            atmosphereRating,
            kindnessRating,
            hygieneRating,
            willRevisit,
            orderId,
            hidden,
            ownerOnly,
            deliveryRating,
            deliveryComment
        );
    }

    void applyChanges(
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
        boolean hidden,
        Integer deliveryRating,
        String deliveryComment
    ) {
        this.content = content;
        this.totalRating = totalRating;
        this.tasteRating = tasteRating;
        this.amountRating = amountRating;
        this.priceRating = priceRating;
        this.atmosphereRating = atmosphereRating;
        this.kindnessRating = kindnessRating;
        this.hygieneRating = hygieneRating;
        this.willRevisit = willRevisit;
        this.hidden = hidden;
        this.deliveryRating = deliveryRating;
        this.deliveryComment = deliveryComment;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public String getContent() {
        return this.content;
    }

    public Double getTotalRating() {
        return this.totalRating;
    }

    public Double getTasteRating() {
        return this.tasteRating;
    }

    public Double getAmountRating() {
        return this.amountRating;
    }

    public Double getPriceRating() {
        return this.priceRating;
    }

    public Double getAtmosphereRating() {
        return this.atmosphereRating;
    }

    public Double getKindnessRating() {
        return this.kindnessRating;
    }

    public Double getHygieneRating() {
        return this.hygieneRating;
    }

    public boolean isWillRevisit() {
        return this.willRevisit;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public boolean isOwnerOnly() {
        return this.ownerOnly;
    }

    public Integer getDeliveryRating() {
        return this.deliveryRating;
    }

    public String getDeliveryComment() {
        return this.deliveryComment;
    }
}
