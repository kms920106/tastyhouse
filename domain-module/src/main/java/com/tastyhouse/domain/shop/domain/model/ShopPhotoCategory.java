package com.tastyhouse.domain.shop.domain.model;

import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 상점 사진 카테고리 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopPhotoCategoryJpaEntity} + {@code ShopPhotoCategoryMapper}가 담당한다.
 */
public class ShopPhotoCategory {

    private final Long id;
    private final ShopId shopId;
    private String name;

    private ShopPhotoCategory(Long id, ShopId shopId, String name) {
        this.id = id;
        this.shopId = shopId;
        this.name = name;
    }

    public static ShopPhotoCategory of(ShopId shopId, String name) {
        return new ShopPhotoCategory(null, shopId, name);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopPhotoCategory reconstitute(Long id, ShopId shopId, String name) {
        return new ShopPhotoCategory(id, shopId, name);
    }

    public void update(String name) {
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getName() {
        return this.name;
    }
}
