package com.tastyhouse.infrastructure.jpa.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.jpa.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_AMENITY_CATEGORY")
class ShopAmenityCategoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amenity", nullable = false, unique = true, length = 50, columnDefinition = "VARCHAR(50)")
    private String amenity;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "active_image_file_id", nullable = false)
    private Long activeImageFileId;

    @Column(name = "inactive_image_file_id", nullable = false)
    private Long inactiveImageFileId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    protected ShopAmenityCategoryJpaEntity() {
    }

    private ShopAmenityCategoryJpaEntity(
        String amenity,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        this.amenity = amenity;
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    static ShopAmenityCategoryJpaEntity create(
        String amenity,
        String displayName,
        Long activeImageFileId,
        Long inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopAmenityCategoryJpaEntity(amenity, displayName, activeImageFileId, inactiveImageFileId, sort, visible);
    }

    void applyChanges(String displayName, Long activeImageFileId, Long inactiveImageFileId, Integer sort, boolean visible) {
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public Long getId() {
        return this.id;
    }

    public String getAmenity() {
        return this.amenity;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Long getActiveImageFileId() {
        return this.activeImageFileId;
    }

    public Long getInactiveImageFileId() {
        return this.inactiveImageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
