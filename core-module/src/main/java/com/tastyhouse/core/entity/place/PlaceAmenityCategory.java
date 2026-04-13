package com.tastyhouse.core.entity.place;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "PLACE_AMENITY_CATEGORY")
public class PlaceAmenityCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "amenity", nullable = false, unique = true, length = 50, columnDefinition = "VARCHAR(50)")
    private Amenity amenity;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "image_url_on", nullable = false)
    private String imageUrlOn;

    @Column(name = "image_url_off", nullable = false)
    private String imageUrlOff;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    private PlaceAmenityCategory(
        Amenity amenity,
        String displayName,
        String imageUrlOn,
        String imageUrlOff,
        Integer sort,
        Boolean isActive
    ) {
        this.amenity = amenity;
        this.displayName = displayName;
        this.imageUrlOn = imageUrlOn;
        this.imageUrlOff = imageUrlOff;
        this.sort = sort;
        this.isActive = isActive;
    }

    public static PlaceAmenityCategory of(
        Amenity amenity,
        String displayName,
        String imageUrlOn,
        String imageUrlOff,
        Integer sort,
        Boolean isActive
    ) {
        return new PlaceAmenityCategory(
            amenity,
            displayName,
            imageUrlOn,
            imageUrlOff,
            sort,
            isActive
        );
    }

    public void update(String displayName, String imageUrlOn, String imageUrlOff, Integer sort, Boolean isActive) {
        this.displayName = displayName;
        this.imageUrlOn = imageUrlOn;
        this.imageUrlOff = imageUrlOff;
        this.sort = sort;
        this.isActive = isActive;
    }
}
