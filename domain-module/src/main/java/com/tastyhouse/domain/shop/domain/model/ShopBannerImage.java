package com.tastyhouse.domain.shop.domain.model;

import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.shop.domain.vo.ShopId;

/**
 * 상점 배너 이미지 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ShopBannerImageJpaEntity} + {@code ShopBannerImageMapper}가 담당한다.
 */
public class ShopBannerImage {

    private final Long id;
    private final ShopId shopId;
    private final UploadedFileId imageFileId;
    private final Integer sort;

    private ShopBannerImage(Long id, ShopId shopId, UploadedFileId imageFileId, Integer sort) {
        this.id = id;
        this.shopId = shopId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    public static ShopBannerImage of(ShopId shopId, UploadedFileId imageFileId, Integer sort) {
        return new ShopBannerImage(null, shopId, imageFileId, sort);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static ShopBannerImage reconstitute(Long id, ShopId shopId, UploadedFileId imageFileId, Integer sort) {
        return new ShopBannerImage(id, shopId, imageFileId, sort);
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public UploadedFileId getImageFileId() {
        return this.imageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }
}
