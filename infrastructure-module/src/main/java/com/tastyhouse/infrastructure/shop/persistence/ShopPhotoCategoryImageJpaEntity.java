package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;
import com.tastyhouse.domain.file.domain.vo.UploadedFileId;
import com.tastyhouse.domain.shop.domain.vo.ShopPhotoCategoryId;
import com.tastyhouse.infrastructure.file.persistence.UploadedFileIdConverter;

/**
 * 상점 사진 카테고리 이미지 JPA 영속 모델. 순수 도메인 모델 {@code ShopPhotoCategoryImage}와 분리된 영속 전용 엔티티다.
 */
@Getter
@Entity
@Table(name = "SHOP_PHOTO_CATEGORY_IMAGE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShopPhotoCategoryImageJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Convert(converter = ShopPhotoCategoryIdConverter.class)
    @Column(name = "shop_photo_category_id", nullable = false)
    private ShopPhotoCategoryId shopPhotoCategoryId; // 사진 카테고리 ID (SHOP_PHOTO_CATEGORY.id 참조)

    @Convert(converter = UploadedFileIdConverter.class)
    @Column(name = "image_file_id", nullable = false)
    private UploadedFileId imageFileId; // 이미지 파일 ID (FILE.id 참조)

    @Column(name = "sort", nullable = false)
    private Integer sort; // 정렬 순서

    @Column(name = "is_visible", nullable = false)
    private boolean visible; // 노출 여부 (true: 노출)

    private ShopPhotoCategoryImageJpaEntity(
        ShopPhotoCategoryId shopPhotoCategoryId,
        UploadedFileId imageFileId,
        Integer sort,
        boolean visible
    ) {
        this.shopPhotoCategoryId = shopPhotoCategoryId;
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    static ShopPhotoCategoryImageJpaEntity create(
        ShopPhotoCategoryId shopPhotoCategoryId,
        UploadedFileId imageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopPhotoCategoryImageJpaEntity(shopPhotoCategoryId, imageFileId, sort, visible);
    }

    void applyChanges(UploadedFileId imageFileId, Integer sort, boolean visible) {
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.visible = visible;
    }
}
