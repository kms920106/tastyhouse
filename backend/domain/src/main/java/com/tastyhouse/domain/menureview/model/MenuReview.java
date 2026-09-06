package com.tastyhouse.domain.menureview.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public class MenuReview {
    private static final int MIN_RATING = 1;

    private static final int MAX_RATING = 5;

    private final Long id;
    private final MemberId memberId;
    private final ShopId shopId;
    private final ProductId productId;
    private final OrderId orderId;

    private final OrderProductId orderProductId;
    private Integer rating;
    private String comment;
    private boolean hidden;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private MenuReview(
        Long id,
        MemberId memberId,
        ShopId shopId,
        ProductId productId,
        OrderId orderId,
        OrderProductId orderProductId,
        Integer rating,
        String comment,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.shopId = shopId;
        this.productId = productId;
        this.orderId = orderId;
        this.orderProductId = orderProductId;
        this.rating = rating;
        this.comment = comment;
        this.hidden = hidden;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MenuReview of(
        MemberId memberId,
        ShopId shopId,
        ProductId productId,
        OrderId orderId,
        OrderProductId orderProductId,
        Integer rating,
        String comment
    ) {
        validateRating(rating);

        return new MenuReview(
            null,
            memberId,
            shopId,
            productId,
            orderId,
            orderProductId,
            rating,
            comment,
            false,
            null,
            null
        );
    }

    public static MenuReview reconstitute(
        Long id,
        MemberId memberId,
        ShopId shopId,
        ProductId productId,
        OrderId orderId,
        OrderProductId orderProductId,
        Integer rating,
        String comment,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new MenuReview(
            id,
            memberId,
            shopId,
            productId,
            orderId,
            orderProductId,
            rating,
            comment,
            hidden,
            createdAt,
            updatedAt
        );
    }

    public void updateRating(Integer rating, String comment) {
        validateRating(rating);

        this.rating = rating;
        this.comment = comment;
    }

    public void hide() {
        this.hidden = true;
    }

    public void unhide() {
        this.hidden = false;
    }

    private static void validateRating(Integer rating) {
        if (rating == null || rating < MIN_RATING || rating > MAX_RATING) {
            throw new BusinessException(ErrorCode.MENU_REVIEW_NOT_ALLOWED,
                ErrorCode.MENU_REVIEW_NOT_ALLOWED.getDefaultMessage() + " 평점: " + rating);
        }
    }

    public Long getId() {
        return this.id;
    }

    public MenuReviewId getMenuReviewId() {
        return MenuReviewId.of(this.id);
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public OrderProductId getOrderProductId() {
        return this.orderProductId;
    }

    public Integer getRating() {
        return this.rating;
    }

    public String getComment() {
        return this.comment;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
