package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SHOP_BANNER_IMAGE")
public class ShopBannerImageJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Column(name = "sort")
    private Integer sort;

    protected ShopBannerImageJpaEntity() {
    }

    private ShopBannerImageJpaEntity(Long shopId, Long imageFileId, Integer sort) {
        this.shopId = shopId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    static ShopBannerImageJpaEntity create(Long shopId, Long imageFileId, Integer sort) {
        return new ShopBannerImageJpaEntity(shopId, imageFileId, sort);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getImageFileId() {
        return this.imageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }
}
