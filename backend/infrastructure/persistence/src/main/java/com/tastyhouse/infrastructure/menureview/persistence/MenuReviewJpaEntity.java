package com.tastyhouse.infrastructure.menureview.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "MENU_REVIEW")
public class MenuReviewJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "order_product_id", nullable = false)
    private Long orderProductId;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", length = 300)
    private String comment;

    @Column(name = "hidden", nullable = false)
    private boolean hidden;

    protected MenuReviewJpaEntity() {
    }

    private MenuReviewJpaEntity(
        Long memberId,
        Long shopId,
        Long productId,
        Long orderId,
        Long orderProductId,
        Integer rating,
        String comment,
        boolean hidden
    ) {
        this.memberId = memberId;
        this.shopId = shopId;
        this.productId = productId;
        this.orderId = orderId;
        this.orderProductId = orderProductId;
        this.rating = rating;
        this.comment = comment;
        this.hidden = hidden;
    }

    static MenuReviewJpaEntity create(
        Long memberId,
        Long shopId,
        Long productId,
        Long orderId,
        Long orderProductId,
        Integer rating,
        String comment,
        boolean hidden
    ) {
        return new MenuReviewJpaEntity(
            memberId,
            shopId,
            productId,
            orderId,
            orderProductId,
            rating,
            comment,
            hidden
        );
    }

    void applyChanges(Integer rating, String comment, boolean hidden) {
        this.rating = rating;
        this.comment = comment;
        this.hidden = hidden;
    }

    public Long getId() {
        return this.id;
    }

    public Long getMemberId() {
        return this.memberId;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public Long getOrderProductId() {
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
}
