package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 상점 사진 카테고리 이미지 JPA 영속 모델. 순수 도메인 모델 {@code ShopPhotoCategoryImage}와 분리된 영속 전용 엔티티다.
 */
@Entity
@Table(name = "SHOP_PHOTO_CATEGORY_IMAGE")
public class ShopPhotoCategoryImageJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_photo_category_id", nullable = false)
    private Long shopPhotoCategoryId; // 사진 카테고리 ID (SHOP_PHOTO_CATEGORY.id 참조)

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId; // 이미지 파일 ID (FILE.id 참조)

    @Column(name = "sort", nullable = false)
    private Integer sort; // 정렬 순서

    @Column(name = "is_visible", nullable = false)
    private boolean visible; // 노출 여부 (true: 노출)

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
