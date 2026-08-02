package com.tastyhouse.domain.shop.domain.model;

import com.tastyhouse.domain.shop.domain.vo.ShopAmenityCategoryId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 상점-편의시설 배정 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopAmenityJpaEntity} + {@code ShopAmenityMapper}가 담당한다.
 */
public class ShopAmenity {

    private final Long id;
    private final ShopId shopId;
    private final ShopAmenityCategoryId shopAmenityCategoryId;

    private ShopAmenity(Long id, ShopId shopId, ShopAmenityCategoryId shopAmenityCategoryId) {
        this.id = id;
        this.shopId = shopId;
        this.shopAmenityCategoryId = shopAmenityCategoryId;
    }

    public static ShopAmenity of(ShopId shopId, ShopAmenityCategoryId shopAmenityCategoryId) {
        return new ShopAmenity(null, shopId, shopAmenityCategoryId);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopAmenity reconstitute(Long id, ShopId shopId, ShopAmenityCategoryId shopAmenityCategoryId) {
        return new ShopAmenity(id, shopId, shopAmenityCategoryId);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ShopAmenityCategoryId getShopAmenityCategoryId() {
        return this.shopAmenityCategoryId;
    }
}
