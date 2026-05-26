package com.tastyhouse.core.domain.place.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PLACE_PHOTO_CATEGORY_IMAGE")
public class PlacePhotoCategoryImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "place_photo_category_id", nullable = false)
    private Long placePhotoCategoryId; // 사진 카테고리 ID (PLACE_PHOTO_CATEGORY.id 참조)

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId; // 이미지 파일 ID (FILE.id 참조)

    @Column(name = "sort", nullable = false)
    private Integer sort; // 정렬 순서

    private PlacePhotoCategoryImage(
        Long placePhotoCategoryId,
        Long imageFileId,
        Integer sort
    ) {
        this.placePhotoCategoryId = placePhotoCategoryId;
        this.imageFileId = imageFileId;
        this.sort = sort;
    }

    public static PlacePhotoCategoryImage of(
        Long placePhotoCategoryId,
        Long imageFileId,
        Integer sort
    ) {
        return new PlacePhotoCategoryImage(
            placePhotoCategoryId,
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
