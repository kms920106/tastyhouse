package com.tastyhouse.domain.shop.domain.model;

import com.tastyhouse.domain.shop.domain.vo.ShopFoodTypeCategoryId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 상점-음식유형 배정 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopFoodTypeJpaEntity} + {@code ShopFoodTypeMapper}가 담당한다.
 */
public class ShopFoodType {

    private final Long id;
    private final ShopId shopId;
    private final ShopFoodTypeCategoryId shopFoodTypeCategoryId;

    private ShopFoodType(Long id, ShopId shopId, ShopFoodTypeCategoryId shopFoodTypeCategoryId) {
        this.id = id;
        this.shopId = shopId;
        this.shopFoodTypeCategoryId = shopFoodTypeCategoryId;
    }

    public static ShopFoodType of(ShopId shopId, ShopFoodTypeCategoryId shopFoodTypeCategoryId) {
        return new ShopFoodType(null, shopId, shopFoodTypeCategoryId);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopFoodType reconstitute(Long id, ShopId shopId, ShopFoodTypeCategoryId shopFoodTypeCategoryId) {
        return new ShopFoodType(id, shopId, shopFoodTypeCategoryId);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ShopFoodTypeCategoryId getShopFoodTypeCategoryId() {
        return this.shopFoodTypeCategoryId;
    }
}
