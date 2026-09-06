package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PRODUCT_PRICE")
public class ProductPriceJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "price_name", length = 50)
    private String priceName;

    @Column(name = "delivery_price", nullable = false)
    private Integer deliveryPrice;

    @Column(name = "store_price")
    private Integer storePrice;

    @Column(name = "pickup_price")
    private Integer pickupPrice;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "pickup_price_set_at")
    private LocalDateTime pickupPriceSetAt;

    protected ProductPriceJpaEntity() {
    }

    private ProductPriceJpaEntity(
        Long productId,
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort,
        LocalDateTime pickupPriceSetAt
    ) {
        this.productId = productId;
        this.priceName = priceName;
        this.deliveryPrice = deliveryPrice;
        this.storePrice = storePrice;
        this.pickupPrice = pickupPrice;
        this.sort = sort;
        this.pickupPriceSetAt = pickupPriceSetAt;
    }

    static ProductPriceJpaEntity create(
        Long productId,
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort,
        LocalDateTime pickupPriceSetAt
    ) {
        return new ProductPriceJpaEntity(
            productId,
            priceName,
            deliveryPrice,
            storePrice,
            pickupPrice,
            sort,
            pickupPriceSetAt
        );
    }

    void applyChanges(
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort,
        LocalDateTime pickupPriceSetAt
    ) {
        this.priceName = priceName;
        this.deliveryPrice = deliveryPrice;
        this.storePrice = storePrice;
        this.pickupPrice = pickupPrice;
        this.sort = sort;
        this.pickupPriceSetAt = pickupPriceSetAt;
    }

    public Long getId() {
        return this.id;
    }

    public Long getProductId() {
        return this.productId;
    }

    public String getPriceName() {
        return this.priceName;
    }

    public Integer getDeliveryPrice() {
        return this.deliveryPrice;
    }

    public Integer getStorePrice() {
        return this.storePrice;
    }

    public Integer getPickupPrice() {
        return this.pickupPrice;
    }

    public Integer getSort() {
        return this.sort;
    }

    public LocalDateTime getPickupPriceSetAt() {
        return this.pickupPriceSetAt;
    }
}
