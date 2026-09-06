package com.tastyhouse.domain.event.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.file.vo.UploadedFileId;

public class Event {
    private final Long id;
    private String name;
    private String description;
    private String subtitle;
    private UploadedFileId thumbnailImageFileId;
    private UploadedFileId bannerImageFileId;
    private String contentHtml;
    private EventStatus status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Event(
        Long id,
        String name,
        String description,
        String subtitle,
        UploadedFileId thumbnailImageFileId,
        UploadedFileId bannerImageFileId,
        String contentHtml,
        EventStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.subtitle = subtitle;
        this.thumbnailImageFileId = thumbnailImageFileId;
        this.bannerImageFileId = bannerImageFileId;
        this.contentHtml = contentHtml;
        this.status = status;
        this.startAt = startAt;
        this.endAt = endAt;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Event of(
        String name,
        String description,
        String subtitle,
        UploadedFileId thumbnailImageFileId,
        UploadedFileId bannerImageFileId,
        String contentHtml,
        EventStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        return new Event(
            null,
            name,
            description,
            subtitle,
            thumbnailImageFileId,
            bannerImageFileId,
            contentHtml,
            status,
            startAt,
            endAt,
            false,
            null,
            null
        );
    }

    public static Event reconstitute(
        Long id,
        String name,
        String description,
        String subtitle,
        UploadedFileId thumbnailImageFileId,
        UploadedFileId bannerImageFileId,
        String contentHtml,
        EventStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Event(
            id,
            name,
            description,
            subtitle,
            thumbnailImageFileId,
            bannerImageFileId,
            contentHtml,
            status,
            startAt,
            endAt,
            deleted,
            createdAt,
            updatedAt
        );
    }

    public EventId getEventId() {
        return EventId.of(this.id);
    }

    public void update(
        String name,
        String description,
        String subtitle,
        UploadedFileId thumbnailImageFileId,
        UploadedFileId bannerImageFileId,
        String contentHtml,
        EventStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        this.name = name;
        this.description = description;
        this.subtitle = subtitle;
        this.thumbnailImageFileId = thumbnailImageFileId;
        this.bannerImageFileId = bannerImageFileId;
        this.contentHtml = contentHtml;
        this.status = status;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void delete() {
        this.deleted = true;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getSubtitle() {
        return this.subtitle;
    }

    public UploadedFileId getThumbnailImageFileId() {
        return this.thumbnailImageFileId;
    }

    public UploadedFileId getBannerImageFileId() {
        return this.bannerImageFileId;
    }

    public String getContentHtml() {
        return this.contentHtml;
    }

    public EventStatus getStatus() {
        return this.status;
    }

    public LocalDateTime getStartAt() {
        return this.startAt;
    }

    public LocalDateTime getEndAt() {
        return this.endAt;
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
