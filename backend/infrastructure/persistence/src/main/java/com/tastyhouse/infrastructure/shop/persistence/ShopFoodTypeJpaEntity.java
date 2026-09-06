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
@Table(name = "SHOP_FOOD_TYPE", uniqueConstraints = {@UniqueConstraint(columnNames = {"shop_id", "shop_food_type_category_id"})})
public class ShopFoodTypeJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "shop_food_type_category_id", nullable = false)
    private Long shopFoodTypeCategoryId;

    protected ShopFoodTypeJpaEntity() {
    }

    private ShopFoodTypeJpaEntity(Long shopId, Long shopFoodTypeCategoryId) {
        this.shopId = shopId;
        this.shopFoodTypeCategoryId = shopFoodTypeCategoryId;
    }

    static ShopFoodTypeJpaEntity create(Long shopId, Long shopFoodTypeCategoryId) {
        return new ShopFoodTypeJpaEntity(shopId, shopFoodTypeCategoryId);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getShopFoodTypeCategoryId() {
        return this.shopFoodTypeCategoryId;
    }
}
