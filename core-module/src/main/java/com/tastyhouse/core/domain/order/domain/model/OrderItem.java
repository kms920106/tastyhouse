package com.tastyhouse.core.domain.order.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "ORDER_ITEM")
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_image_url", length = 500)
    private String productImageUrl;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice;

    @Column(name = "discount_price")
    private Integer discountPrice;

    @Column(name = "option_total_price", nullable = false)
    private Integer optionTotalPrice;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    private OrderItem(
        Long orderId,
        Long productId,
        String productName,
        String productImageUrl,
        Integer quantity,
        Integer unitPrice,
        Integer discountPrice,
        Integer optionTotalPrice,
        Integer totalPrice
    ) {
        this.orderId = orderId;
        this.productId = productId;
        this.productName = productName;
        this.productImageUrl = productImageUrl;
        this.quantity = quantity != null ? quantity : 1;
        this.unitPrice = unitPrice != null ? unitPrice : 0;
        this.discountPrice = discountPrice;
        this.optionTotalPrice = optionTotalPrice != null ? optionTotalPrice : 0;
        this.totalPrice = totalPrice != null ? totalPrice : 0;
    }

    public static OrderItem of(
        Long orderId,
        Long productId,
        String productName,
        String productImageUrl,
        Integer quantity,
        Integer unitPrice,
        Integer discountPrice,
        Integer optionTotalPrice,
        Integer totalPrice
    ) {
        return new OrderItem(
            orderId,
            productId,
            productName,
            productImageUrl,
            quantity,
            unitPrice,
            discountPrice,
            optionTotalPrice,
            totalPrice
        );
    }

    public void updatePrices(Integer optionTotalPrice, Integer totalPrice) {
        this.optionTotalPrice = optionTotalPrice;
        this.totalPrice = totalPrice;
    }
}
