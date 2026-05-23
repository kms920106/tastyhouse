package com.tastyhouse.core.entity.order;

import com.tastyhouse.core.entity.BaseEntity;
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
    private Long id; // PK

    @Column(name = "order_id", nullable = false)
    private Long orderId; // 주문 ID (ORDERS.id 참조)

    @Column(name = "product_id", nullable = false)
    private Long productId; // 상품 ID (PRODUCT.id 참조)

    @Column(name = "product_name", nullable = false)
    private String productName; // 주문 당시 상품명 (스냅샷)

    @Column(name = "product_image_url", length = 500)
    private String productImageUrl; // 주문 당시 상품 이미지 URL (스냅샷)

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // 주문 수량

    @Column(name = "unit_price", nullable = false)
    private Integer unitPrice; // 단위 상품 가격 (할인 전)

    @Column(name = "discount_price")
    private Integer discountPrice; // 상품 할인 금액 (null이면 할인 없음)

    @Column(name = "option_total_price", nullable = false)
    private Integer optionTotalPrice; // 선택 옵션 추가 금액 합계

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice; // 이 주문 항목의 최종 금액 합계

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

    public void updatePrices(
        Integer optionTotalPrice,
        Integer totalPrice
    ) {
        this.optionTotalPrice = optionTotalPrice;
        this.totalPrice = totalPrice;
    }
}
