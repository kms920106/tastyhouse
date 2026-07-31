package com.tastyhouse.domain.shop.domain.model;

import lombok.Getter;

/**
 * 편의시설 카테고리 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopAmenityCategoryJpaEntity} + {@code ShopAmenityCategoryMapper}가 담당한다.
 */
@Getter
public class ShopAmenityCategory {

    private final Long id;
    private final Amenity amenity;
    private String displayName;
    private Long activeImageFileId;
    private Long inactiveImageFileId;
    private Integer sort;
    private boolean visible;

    private ShopAmenityCategory(
        Long id,
        Amenity amenity,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        this.id = id;
        this.amenity = amenity;
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public static ShopAmenityCategory of(
        Amenity amenity,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopAmenityCategory(null, amenity, displayName, activeImageFileId, inactiveImageFileId, sort, visible);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopAmenityCategory reconstitute(
        Long id,
        Amenity amenity,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopAmenityCategory(id, amenity, displayName, activeImageFileId, inactiveImageFileId, sort, visible);
    }

    public void update(String displayName, Long activeImageFileId, Long inactiveImageFileId, Integer sort, boolean visible) {
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.visible = visible;
    }
}
