package com.tastyhouse.core.domain.shop.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "SHOP_PHOTO_CATEGORY_IMAGE")
public class ShopPhotoCategoryImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "shop_photo_category_id", nullable = false)
    private Long shopPhotoCategoryId; // 사진 카테고리 ID (SHOP_PHOTO_CATEGORY.id 참조)

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId; // 이미지 파일 ID (FILE.id 참조)

    @Column(name = "sort", nullable = false)
    private Integer sort; // 정렬 순서

    private ShopPhotoCategoryImage(
        Long shopPhotoCategoryId,
        Long imageFileId,
        Integer sort
    ) {
        this.shopPhotoCategoryId = shopPhotoCategoryId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    public static ShopPhotoCategoryImage of(
        Long shopPhotoCategoryId,
        Long imageFileId,
        Integer sort
    ) {
        return new ShopPhotoCategoryImage(
            shopPhotoCategoryId,
            imageFileId,
            sort
        );
    }

    public void update(
        Long imageFileId,
        Integer sort
    ) {
        this.imageFileId = imageFileId;
        this.sort = sort;
    }
}
