package com.tastyhouse.core.entity.place;

import com.tastyhouse.core.entity.BaseEntity;
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
    private Long id;

    @Column(name = "place_photo_category_id", nullable = false)
    private Long placePhotoCategoryId; // 이미지 카테고리 ID

    @Column(name = "image_url", nullable = false)
    private String imageUrl; // 이미지 URL

    @Column(name = "sort", nullable = false)
    private Integer sort; // 정렬 순서

    private PlacePhotoCategoryImage(
        Long placePhotoCategoryId,
        String imageUrl,
        Integer sort
    ) {
        this.placePhotoCategoryId = placePhotoCategoryId;
        this.imageUrl = imageUrl;
        this.sort = sort;
    }

    public static PlacePhotoCategoryImage of(
        Long placePhotoCategoryId,
        String imageUrl,
        Integer sort
    ) {
        return new PlacePhotoCategoryImage(
            placePhotoCategoryId,
            imageUrl,
            sort
        );
    }

    public void update(
        String imageUrl,
        Integer sort
    ) {
        this.imageUrl = imageUrl;
        this.sort = sort;
    }
}
