package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_PHOTO_CATEGORY_IMAGE")
public class ShopPhotoCategoryImageJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_photo_category_id", nullable = false)
    private Long shopPhotoCategoryId;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    protected ShopPhotoCategoryImageJpaEntity() {
    }

    private ShopPhotoCategoryImageJpaEntity(
        Long shopPhotoCategoryId,
        Long imageFileId,
        Integer sort,
        boolean visible
    ) {
        this.shopPhotoCategoryId = shopPhotoCategoryId;
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    static ShopPhotoCategoryImageJpaEntity create(
        Long shopPhotoCategoryId,
        Long imageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopPhotoCategoryImageJpaEntity(shopPhotoCategoryId, imageFileId, sort, visible);
    }

    void applyChanges(Long imageFileId, Integer sort, boolean visible) {
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopPhotoCategoryId() {
        return this.shopPhotoCategoryId;
    }

    public Long getImageFileId() {
        return this.imageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
