package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_PHOTO_CATEGORY")
public class ShopPhotoCategoryJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    protected ShopPhotoCategoryJpaEntity() {
    }

    private ShopPhotoCategoryJpaEntity(Long shopId, String name) {
        this.shopId = shopId;
        this.name = name;
    }

    static ShopPhotoCategoryJpaEntity create(Long shopId, String name) {
        return new ShopPhotoCategoryJpaEntity(shopId, name);
    }

    void applyChanges(String name) {
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getName() {
        return this.name;
    }
}
