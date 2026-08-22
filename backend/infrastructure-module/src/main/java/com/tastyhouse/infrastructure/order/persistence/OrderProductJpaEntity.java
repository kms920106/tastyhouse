package com.tastyhouse.infrastructure.order.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 주문 상품 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code OrderProduct}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code OrderProductMapper}가 수행한다.
 */
@Entity
@Table(name = "ORDER_PRODUCT")
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

    @Column(name = "image_file_id")
    private Long imageFileId; // 주문 시점 상품 이미지 파일 ID (UPLOADED_FILE.id 스냅샷). 이미지 없으면 null

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // 수량

    @Column(name = "original_price", nullable = false)
    private Integer originalPrice; // 정가

    @Column(name = "discount_price")
    private Integer discountPrice; // 할인가

    @Column(name = "total_option_price", nullable = false)
    private Integer totalOptionPrice; // 옵션 금액 합계

    /**
     * 이 라인의 일회용컵 보증금 합계(수량 반영). {@code total_option_price}·{@code total_price}에는
     * 포함되지 않는다 — 포함하면 주문 전체의 상품 금액으로 흘러들어 최소주문금액·쿠폰·포인트 기준액이
     * 오염된다.
     */
    @Column(name = "cup_deposit_amount", nullable = false)
    private Integer cupDepositAmount;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice; // 상품 총 금액

    protected OrderProductJpaEntity() {
    }

    private OrderProductJpaEntity(
        Long orderId,
        Long productId,
        String name,
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
        this.imageFileId = imageFileId;
        this.quantity = quantity;
        this.originalPrice = originalPrice;
        this.discountPrice = discountPrice;
        this.totalOptionPrice = totalOptionPrice;
        this.totalPrice = totalPrice;
        this.cupDepositAmount = cupDepositAmount;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code OrderProductMapper#toEntity}에서만 호출한다.
     */
    static OrderProductJpaEntity create(
        Long orderId,
        Long productId,
        String name,
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
            imageFileId,
            quantity,
            originalPrice,
            discountPrice,
            totalOptionPrice,
            totalPrice,
            cupDepositAmount
        );
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
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
