package com.tastyhouse.core.domain.shop.domain.model;

import lombok.Getter;

/**
 * 상점-음식유형 배정 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopFoodTypeJpaEntity} + {@code ShopFoodTypeMapper}가 담당한다.
 */
@Getter
public class ShopFoodType {

    private final Long id;
    private final Long shopId;
    private final Long shopFoodTypeCategoryId;

    private ShopFoodType(Long id, Long shopId, Long shopFoodTypeCategoryId) {
        this.id = id;
        this.shopId = shopId;
        this.shopFoodTypeCategoryId = shopFoodTypeCategoryId;
    }

    public static ShopFoodType of(Long shopId, Long shopFoodTypeCategoryId) {
        return new ShopFoodType(null, shopId, shopFoodTypeCategoryId);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopFoodType reconstitute(Long id, Long shopId, Long shopFoodTypeCategoryId) {
        return new ShopFoodType(id, shopId, shopFoodTypeCategoryId);
    }
}
