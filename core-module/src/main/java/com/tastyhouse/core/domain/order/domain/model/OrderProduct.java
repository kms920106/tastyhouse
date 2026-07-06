package com.tastyhouse.core.domain.order.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.order.domain.vo.OrderProductId;
import com.tastyhouse.core.shared.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "ORDER_PRODUCT")
public class OrderProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "original_price", nullable = false)
    private Integer originalPrice;

    @Column(name = "discount_price")
    private Integer discountPrice;

    @Column(name = "total_option_price", nullable = false)
    private Integer totalOptionPrice;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    private OrderProduct(
        Long orderId,
        Long productId,
        String name,
        String imageUrl,
        Integer quantity,
        Integer originalPrice,
        Integer discountPrice,
        Integer totalOptionPrice,
        Integer totalPrice
    ) {
        this.orderId = orderId;
        this.productId = productId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.quantity = quantity != null ? quantity : 1;
        this.originalPrice = originalPrice != null ? originalPrice : 0;
        this.discountPrice = discountPrice;
        this.totalOptionPrice = totalOptionPrice != null ? totalOptionPrice : 0;
        this.totalPrice = totalPrice != null ? totalPrice : 0;
    }

    public static OrderProduct of(
        Long orderId,
        Long productId,
        String name,
        String imageUrl,
        Integer quantity,
        Integer originalPrice,
        Integer discountPrice,
        Integer totalOptionPrice,
        Integer totalPrice
    ) {
        return new OrderProduct(
            orderId,
            productId,
            name,
            imageUrl,
            quantity,
            originalPrice,
            discountPrice,
            totalOptionPrice,
            totalPrice
        );
    }

    public void updatePrices(Integer totalOptionPrice, Integer totalPrice) {
        this.totalOptionPrice = totalOptionPrice;
        this.totalPrice = totalPrice;
    }

    public OrderProductId getOrderProductId() {
        return OrderProductId.of(this.id);
    }
}
