package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;
import com.tastyhouse.domain.shop.domain.vo.ShopFoodTypeCategoryId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 상점-음식유형 배정 JPA 영속 모델. 순수 도메인 모델 {@code ShopFoodType}과 분리된 영속 전용 엔티티다.
 */
@Getter
@Entity
@Table(name = "SHOP_FOOD_TYPE", uniqueConstraints = {@UniqueConstraint(columnNames = {"shop_id", "shop_food_type_category_id"})})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopFoodTypeJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Convert(converter = ShopIdConverter.class)
    @Column(name = "shop_id", nullable = false)
    private ShopId shopId; // 가게 ID (SHOP.id 참조)

    @Convert(converter = ShopFoodTypeCategoryIdConverter.class)
    @Column(name = "shop_food_type_category_id", nullable = false)
    private ShopFoodTypeCategoryId shopFoodTypeCategoryId; // 음식 유형 카테고리 ID (SHOP_FOOD_TYPE_CATEGORY.id 참조)

    private ShopFoodTypeJpaEntity(ShopId shopId, ShopFoodTypeCategoryId shopFoodTypeCategoryId) {
        this.shopId = shopId;
        this.shopFoodTypeCategoryId = shopFoodTypeCategoryId;
    }

    static ShopFoodTypeJpaEntity create(ShopId shopId, ShopFoodTypeCategoryId shopFoodTypeCategoryId) {
        return new ShopFoodTypeJpaEntity(shopId, shopFoodTypeCategoryId);
    }
}
