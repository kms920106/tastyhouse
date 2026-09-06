package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_AMENITY", uniqueConstraints = {@UniqueConstraint(columnNames = {"shop_id", "shop_amenity_category_id"})})
public class ShopAmenityJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "shop_amenity_category_id", nullable = false)
    private Long shopAmenityCategoryId;

    protected ShopAmenityJpaEntity() {
    }

    private ShopAmenityJpaEntity(Long shopId, Long shopAmenityCategoryId) {
        this.shopId = shopId;
        this.shopAmenityCategoryId = shopAmenityCategoryId;
    }

    static ShopAmenityJpaEntity create(Long shopId, Long shopAmenityCategoryId) {
        return new ShopAmenityJpaEntity(shopId, shopAmenityCategoryId);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getShopAmenityCategoryId() {
        return this.shopAmenityCategoryId;
    }
}
