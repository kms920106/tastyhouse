package com.tastyhouse.domain.shop.model;

import com.tastyhouse.domain.file.vo.UploadedFileId;

public class ShopAmenityCategory {
    private final Long id;
    private final Amenity amenity;
    private String displayName;
    private UploadedFileId activeImageFileId;
    private UploadedFileId inactiveImageFileId;
    private Integer sort;
    private boolean visible;

    private ShopAmenityCategory(
        Long id,
        Amenity amenity,
        String displayName,
        UploadedFileId activeImageFileId,
        UploadedFileId inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        this.id = id;
        this.amenity = amenity;
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public static ShopAmenityCategory of(
        Amenity amenity,
        String displayName,
        UploadedFileId activeImageFileId,
        UploadedFileId inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopAmenityCategory(null, amenity, displayName, activeImageFileId, inactiveImageFileId, sort, visible);
    }

    public static ShopAmenityCategory reconstitute(
        Long id,
        Amenity amenity,
        String displayName,
        UploadedFileId activeImageFileId,
        UploadedFileId inactiveImageFileId,
        Integer sort,
        boolean visible
    ) {
        return new ShopAmenityCategory(id, amenity, displayName, activeImageFileId, inactiveImageFileId, sort, visible);
    }

    public void update(String displayName, UploadedFileId activeImageFileId, UploadedFileId inactiveImageFileId, Integer sort, boolean visible) {
        this.displayName = displayName;
        this.activeImageFileId = activeImageFileId;
        this.inactiveImageFileId = inactiveImageFileId;
        this.sort = sort;
        this.visible = visible;
    }

    public Long getId() {
        return this.id;
    }

    public Amenity getAmenity() {
        return this.amenity;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public UploadedFileId getActiveImageFileId() {
        return this.activeImageFileId;
    }

    public UploadedFileId getInactiveImageFileId() {
        return this.inactiveImageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
