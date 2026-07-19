package com.tastyhouse.infrastructure.order.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

/**
 * 주문 상품 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code OrderProduct}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code OrderProductMapper}가 수행한다.
 */
@Getter
@Entity
@Table(name = "ORDER_PRODUCT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProductJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "order_id", nullable = false)
    private Long orderId; // 주문 ID (ORDERS.id 참조)

    @Column(name = "product_id", nullable = false)
    private Long productId; // 상품 ID (PRODUCT.id 참조)

    @Column(name = "name", nullable = false)
    private String name; // 주문 시점 상품명 (스냅샷)

    @Column(name = "image_url", length = 500)
    private String imageUrl; // 주문 시점 상품 이미지 URL (스냅샷)

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // 수량

    @Column(name = "original_price", nullable = false)
    private Integer originalPrice; // 정가

    @Column(name = "discount_price")
    private Integer discountPrice; // 할인가

    @Column(name = "total_option_price", nullable = false)
    private Integer totalOptionPrice; // 옵션 금액 합계

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice; // 상품 총 금액

    private OrderProductJpaEntity(
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
        this.quantity = quantity;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.totalOptionPrice = totalOptionPrice;
        this.totalPrice = totalPrice;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code OrderProductMapper#toEntity}에서만 호출한다.
     */
    static OrderProductJpaEntity create(
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
        return new OrderProductJpaEntity(
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

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(Integer totalOptionPrice, Integer totalPrice) {
        this.totalOptionPrice = totalOptionPrice;
        this.totalPrice = totalPrice;
    }
}
