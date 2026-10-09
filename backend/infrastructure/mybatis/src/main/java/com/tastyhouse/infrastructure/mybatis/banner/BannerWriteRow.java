package com.tastyhouse.infrastructure.mybatis.banner;

import java.time.LocalDateTime;

final class BannerWriteRow {

    private Long id;
    private final String type;
    private final String title;
    private final Long imageFileId;
    private final String linkUrl;
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final Integer sort;
    private final boolean visible;
    private final boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    BannerWriteRow(
        Long id,
        String type,
        String title,
        Long imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.imageFileId = imageFileId;
        this.linkUrl = linkUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public String getTitle() {
        return this.title;
    }

    public Long getImageFileId() {
        return this.imageFileId;
    }

    public String getLinkUrl() {
        return this.linkUrl;
    }

    public LocalDateTime getStartDate() {
        return this.startDate;
    }

    public LocalDateTime getEndDate() {
        return this.endDate;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
