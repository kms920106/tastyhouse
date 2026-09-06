package com.tastyhouse.domain.banner.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.banner.vo.BannerId;
import com.tastyhouse.domain.file.vo.UploadedFileId;

public class Banner {
    private final Long id;
    private BannerType type;
    private String title;
    private UploadedFileId imageFileId;
    private String linkUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer sort;
    private boolean visible;
    private boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Banner(
        Long id,
        BannerType type,
        String title,
        UploadedFileId imageFileId,
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

    public static Banner of(
        BannerType type,
        String title,
        UploadedFileId imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible
    ) {
        return new Banner(null, type, title, imageFileId, linkUrl, startDate, endDate, sort, visible, false, null, null);
    }

    public static Banner reconstitute(
        Long id,
        BannerType type,
        String title,
        UploadedFileId imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Banner(id, type, title, imageFileId, linkUrl, startDate, endDate, sort, visible, deleted, createdAt, updatedAt);
    }

    public BannerId getBannerId() {
        return BannerId.of(this.id);
    }

    public void update(
        BannerType type,
        String title,
        UploadedFileId imageFileId,
        String linkUrl,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Integer sort,
        boolean visible
    ) {
        this.type = type;
        this.title = title;
        this.imageFileId = imageFileId;
        this.linkUrl = linkUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.sort = sort;
        this.visible = visible;
    }

    public void delete() {
        this.deleted = true;
    }

    public Long getId() {
        return this.id;
    }

    public BannerType getType() {
        return this.type;
    }

    public String getTitle() {
        return this.title;
    }

    public UploadedFileId getImageFileId() {
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
