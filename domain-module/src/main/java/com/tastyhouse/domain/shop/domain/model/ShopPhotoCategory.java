package com.tastyhouse.domain.shop.domain.model;

import lombok.Getter;

/**
 * 상점 사진 카테고리 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopPhotoCategoryJpaEntity} + {@code ShopPhotoCategoryMapper}가 담당한다.
 */
@Getter
public class ShopPhotoCategory {

    private final Long id;
    private final Long shopId;
    private String name;

    private ShopPhotoCategory(Long id, Long shopId, String name) {
        this.id = id;
        this.shopId = shopId;
        this.name = name;
    }

    public static ShopPhotoCategory of(Long shopId, String name) {
        return new ShopPhotoCategory(null, shopId, name);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopPhotoCategory reconstitute(Long id, Long shopId, String name) {
        return new ShopPhotoCategory(id, shopId, name);
    }

    public void update(String name) {
        this.name = name;
    }
}
