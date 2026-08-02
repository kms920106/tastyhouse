package com.tastyhouse.domain.shop.domain.model;

import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.shop.domain.vo.ShopPhotoCategoryId;

/**
 * 상점 사진 카테고리 이미지 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopPhotoCategoryImageJpaEntity} + {@code ShopPhotoCategoryImageMapper}가 담당한다.
 */
public class ShopPhotoCategoryImage {

    private final Long id;
    private final ShopPhotoCategoryId shopPhotoCategoryId;
    private UploadedFileId imageFileId;
    private Integer sort;
    private boolean visible;

    private ShopPhotoCategoryImage(
        Long id,
        ShopPhotoCategoryId shopPhotoCategoryId,
        UploadedFileId imageFileId,
        Integer sort,
        boolean visible
    ) {
        this.id = id;
        this.shopPhotoCategoryId = shopPhotoCategoryId;
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public static ShopPhotoCategoryImage of(
        ShopPhotoCategoryId shopPhotoCategoryId,
        UploadedFileId imageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopPhotoCategoryImage(null, shopPhotoCategoryId, imageFileId, sort, visible);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopPhotoCategoryImage reconstitute(
        Long id,
        ShopPhotoCategoryId shopPhotoCategoryId,
        UploadedFileId imageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopPhotoCategoryImage(id, shopPhotoCategoryId, imageFileId, sort, visible);
    }

    public void update(UploadedFileId imageFileId, Integer sort, boolean visible) {
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public Long getId() {
        return this.id;
    }

    public ShopPhotoCategoryId getShopPhotoCategoryId() {
        return this.shopPhotoCategoryId;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
