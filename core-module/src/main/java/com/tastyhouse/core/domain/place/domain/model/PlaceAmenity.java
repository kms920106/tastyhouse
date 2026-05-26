package com.tastyhouse.core.domain.place.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "PLACE_AMENITY", uniqueConstraints = {@UniqueConstraint(columnNames = {"place_id", "place_amenity_category_id"})})
public class PlaceAmenity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "place_id", nullable = false)
    private Long placeId; // 장소 ID (PLACE.id 참조)

    @Column(name = "place_amenity_category_id", nullable = false)
    private Long placeAmenityCategoryId; // 편의시설 카테고리 ID (PLACE_AMENITY_CATEGORY.id 참조)

    private PlaceAmenity(
        Long placeId,
        Long placeAmenityCategoryId
    ) {
        this.placeId = placeId;
        this.placeAmenityCategoryId = placeAmenityCategoryId;
    }

    public static PlaceAmenity of(
        Long placeId,
        Long placeAmenityCategoryId
    ) {
        return new PlaceAmenity(
            placeId,
            placeAmenityCategoryId
        );
    }
}
