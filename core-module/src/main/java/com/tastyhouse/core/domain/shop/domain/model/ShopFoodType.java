package com.tastyhouse.core.domain.shop.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "SHOP_FOOD_TYPE", uniqueConstraints = {@UniqueConstraint(columnNames = {"shop_id", "shop_food_type_category_id"})})
public class ShopFoodType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_id", nullable = false)
    private Long shopId; // 가게 ID (SHOP.id 참조)

    @Column(name = "shop_food_type_category_id", nullable = false)
    private Long shopFoodTypeCategoryId; // 음식 유형 카테고리 ID (SHOP_FOOD_TYPE_CATEGORY.id 참조)

    private ShopFoodType(
        Long shopId,
        Long shopFoodTypeCategoryId
    ) {
        this.shopId = shopId;
        this.shopFoodTypeCategoryId = shopFoodTypeCategoryId;
    }

    public static ShopFoodType of(
        Long shopId,
        Long shopFoodTypeCategoryId
    ) {
        return new ShopFoodType(
            shopId,
            shopFoodTypeCategoryId
        );
    }
}
