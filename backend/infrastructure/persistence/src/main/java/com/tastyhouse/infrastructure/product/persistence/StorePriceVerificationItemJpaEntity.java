package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 매장 가격 인증 요청 항목 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code StorePriceVerificationItem}과 분리된 영속 전용 엔티티다. 도메인↔엔티티
 * 변환은 {@code StorePriceVerificationItemMapper}가 수행한다.
 *
 * <p>{@code store_price}를 항목이 직접 들고 있는 이유는 <b>승인이 요청 시점의 매장가를 쓴다</b>는
 * 규칙 때문이다 — 승인 시점에 현재 가격을 다시 읽으면 검수자가 보지 않은 값이 승인된다.
 *
 * <p>테이블명의 {@code SHOP_} 접두는 부모 요청 테이블과 짝을 맞춘 것이며 소유 컨텍스트(product)와는
 * 별개다.
 */
@Entity
@Table(name = "SHOP_STORE_PRICE_VERIFICATION_ITEM")
public class StorePriceVerificationItemJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_id", nullable = false)
    private Long verificationId;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_price_id", nullable = false)
    private Long productPriceId;

    @Column(name = "store_price", nullable = false)
    private Integer storePrice; // 요청한 매장 가격 — 승인 시 반영될 값

    @Column(name = "apply_pickup_same_price", nullable = false)
    private boolean applyPickupSamePrice;

    protected StorePriceVerificationItemJpaEntity() {
    }

    private StorePriceVerificationItemJpaEntity(
        Long verificationId,
        Long productId,
        Long productPriceId,
        Integer storePrice,
        boolean applyPickupSamePrice
    ) {
        this.verificationId = verificationId;
        this.productId = productId;
        this.productPriceId = productPriceId;
        this.storePrice = storePrice;
        this.applyPickupSamePrice = applyPickupSamePrice;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code StorePriceVerificationItemMapper#toEntity}에서만
     * 호출한다.
     *
     * <p>항목은 접수 후 변경되지 않으므로 {@code applyChanges}를 두지 않는다(update 경로 없음).
     */
    static StorePriceVerificationItemJpaEntity create(
        Long verificationId,
        Long productId,
        Long productPriceId,
        Integer storePrice,
        boolean applyPickupSamePrice
    ) {
        return new StorePriceVerificationItemJpaEntity(
            verificationId,
            productId,
            productPriceId,
            storePrice,
            applyPickupSamePrice
        );
    }

    public Long getId() {
        return this.id;
    }

    public Long getVerificationId() {
        return this.verificationId;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Long getProductPriceId() {
        return this.productPriceId;
    }

    public Integer getStorePrice() {
        return this.storePrice;
    }

    public boolean isApplyPickupSamePrice() {
        return this.applyPickupSamePrice;
    }
}
