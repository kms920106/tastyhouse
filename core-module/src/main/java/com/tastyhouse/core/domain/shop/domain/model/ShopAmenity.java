package com.tastyhouse.core.domain.shop.domain.model;

import lombok.Getter;

/**
 * 상점-편의시설 배정 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopAmenityJpaEntity} + {@code ShopAmenityMapper}가 담당한다.
 */
@Getter
public class ShopAmenity {

    private final Long id;
    private final Long shopId;
    private final Long shopAmenityCategoryId;

    private ShopAmenity(Long id, Long shopId, Long shopAmenityCategoryId) {
        this.id = id;
        this.shopId = shopId;
        this.shopAmenityCategoryId = shopAmenityCategoryId;
    }

    public static ShopAmenity of(Long shopId, Long shopAmenityCategoryId) {
        return new ShopAmenity(null, shopId, shopAmenityCategoryId);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopAmenity reconstitute(Long id, Long shopId, Long shopAmenityCategoryId) {
        return new ShopAmenity(id, shopId, shopAmenityCategoryId);
    }
}
