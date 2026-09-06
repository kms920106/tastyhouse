package com.tastyhouse.infrastructure.order.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "ORDER_PRODUCT")
public class OrderProductJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price_name", length = 50)
    private String priceName;

    @Column(name = "image_file_id")
    private Long imageFileId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "original_price", nullable = false)
    private Integer originalPrice;

    @Column(name = "discount_price")
    private Integer discountPrice;

    @Column(name = "total_option_price", nullable = false)
    private Integer totalOptionPrice;

    @Column(name = "cup_deposit_amount", nullable = false)
    private Integer cupDepositAmount;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    protected OrderProductJpaEntity() {
    }

    private OrderProductJpaEntity(
        Long orderId,
        Long productId,
        String name,
        String priceName,
        Long imageFileId,
        Integer quantity,
        Integer originalPrice,
        Integer discountPrice,
        Integer totalOptionPrice,
        Integer totalPrice,
        Integer cupDepositAmount
    ) {
        this.orderId = orderId;
        this.productId = productId;
        this.name = name;
        this.priceName = priceName;
        this.imageFileId = imageFileId;
        this.quantity = quantity;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.totalOptionPrice = totalOptionPrice;
        this.totalPrice = totalPrice;
        this.cupDepositAmount = cupDepositAmount;
    }

    static OrderProductJpaEntity create(
        Long orderId,
        Long productId,
        String name,
        String priceName,
        Long imageFileId,
        Integer quantity,
        Integer originalPrice,
        Integer discountPrice,
        Integer totalOptionPrice,
        Integer totalPrice,
        Integer cupDepositAmount
    ) {
        return new OrderProductJpaEntity(
            orderId,
            productId,
            name,
            priceName,
            imageFileId,
            quantity,
            originalPrice,
            discountPrice,
            totalOptionPrice,
            totalPrice,
            cupDepositAmount
        );
    }

    void applyChanges(Integer totalOptionPrice, Integer totalPrice, Integer cupDepositAmount) {
        this.totalOptionPrice = totalOptionPrice;
        this.totalPrice = totalPrice;
        this.cupDepositAmount = cupDepositAmount;
    }

    public Long getId() {
        return this.id;
    }

    public Long getOrderId() {
        return this.orderId;
    }

    public Long getProductId() {
        return this.productId;
    }

    public String getName() {
        return this.name;
    }

    public String getPriceName() {
        return this.priceName;
    }

    public Long getImageFileId() {
        return this.imageFileId;
    }

    public Integer getQuantity() {
        return this.quantity;
    }

    public Integer getOriginalPrice() {
        return this.originalPrice;
    }

    public Integer getDiscountPrice() {
        return this.discountPrice;
    }

    public Integer getTotalOptionPrice() {
        return this.totalOptionPrice;
    }

    public Integer getTotalPrice() {
        return this.totalPrice;
    }

    public Integer getCupDepositAmount() {
        return this.cupDepositAmount;
    }
}
