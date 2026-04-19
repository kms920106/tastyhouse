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

    @Column(name = "active_image_file_id", nullable = false)
    private Long activeImageFileId;

    @Column(name = "inactive_image_file_id", nullable = false)
    private Long inactiveImageFileId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    private PlaceAmenityCategory(
        Amenity amenity,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        Boolean isActive
    ) {
        this.amenity = amenity;
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.isActive = isActive;
    }

    public static PlaceAmenityCategory of(
        Amenity amenity,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        Boolean isActive
    ) {
        return new PlaceAmenityCategory(
            amenity,
            displayName,
            activeImageFileId,
            inactiveImageFileId,
            sort,
            isActive
        );
    }

    public void update(String displayName, Long activeImageFileId, Long inactiveImageFileId, Integer sort, Boolean isActive) {
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.isActive = isActive;
    }
}
