package com.tastyhouse.infrastructure.product.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

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
    private Integer storePrice;

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
