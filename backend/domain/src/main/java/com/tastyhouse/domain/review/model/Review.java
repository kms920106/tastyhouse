package com.tastyhouse.domain.review.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.shop.vo.ShopId;

public class Review {
    private final Long id;
    private final ShopId shopId;
    private final ProductId productId;
    private final MemberId memberId;
    private String content;
    private Double totalRating;
    private Double tasteRating;
    private Double amountRating;
    private Double priceRating;
    private Double atmosphereRating;
    private Double kindnessRating;
    private Double hygieneRating;
    private boolean willRevisit;
    private final OrderId orderId;
    private boolean hidden;

    private final boolean ownerOnly;

    private Integer deliveryRating;

    private String deliveryComment;
    private final LocalDateTime createdAt;

    private Review(
        Long id,
        ShopId shopId,
        ProductId productId,
        MemberId memberId,
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
        OrderId orderId,
        boolean hidden,
        boolean ownerOnly,
        Integer deliveryRating,
        String deliveryComment,
        LocalDateTime createdAt
    ) {
        this.id = id;
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
        this.createdAt = createdAt;
    }

    public static Review of(
        ShopId shopId,
        ProductId productId,
        MemberId memberId,
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
        OrderId orderId,
        boolean ownerOnly,
        Integer deliveryRating,
        String deliveryComment
    ) {
        return new Review(
            null,
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
            false,
            ownerOnly,
            deliveryRating,
            deliveryComment,
            null
        );
    }

    public static Review reconstitute(
        Long id,
        ShopId shopId,
        ProductId productId,
        MemberId memberId,
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
        OrderId orderId,
        boolean hidden,
        boolean ownerOnly,
        Integer deliveryRating,
        String deliveryComment,
        LocalDateTime createdAt
    ) {
        return new Review(
            id,
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
            deliveryComment,
            createdAt
        );
    }

    public ReviewId getReviewId() {
        return ReviewId.of(this.id);
    }

    public void hide() {
        this.hidden = true;
    }

    public void unhide() {
        this.hidden = false;
    }

    public void updateContent(
        String content,
        Double totalRating,
        Double tasteRating,
        Double amountRating,
        Double priceRating,
        Double atmosphereRating,
        Double kindnessRating,
        Double hygieneRating,
        boolean willRevisit,
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
        this.deliveryRating = deliveryRating;
        this.deliveryComment = deliveryComment;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public MemberId getMemberId() {
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

    public OrderId getOrderId() {
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
