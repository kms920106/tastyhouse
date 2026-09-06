package com.tastyhouse.infrastructure.event.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "EVENT",
    indexes = {
        @Index(name = "idx_event_active", columnList = "is_deleted, status"),
        @Index(name = "idx_event_period", columnList = "start_at, end_at")
    }
)
public class EventJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "subtitle", length = 200)
    private String subtitle;

    @Column(name = "thumbnail_image_file_id")
    private Long thumbnailImageFileId;

    @Column(name = "banner_image_file_id")
    private Long bannerImageFileId;

    @Column(name = "content_html", columnDefinition = "TEXT")
    private String contentHtml;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private EventStatus status;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    protected EventJpaEntity() {
    }

    private EventJpaEntity(
        String name,
        String description,
        String subtitle,
        Long thumbnailImageFileId,
        Long bannerImageFileId,
        String contentHtml,
        EventStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean deleted
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
        this.deleted = deleted;
    }

    static EventJpaEntity create(
        String name,
        String description,
        String subtitle,
        Long thumbnailImageFileId,
        Long bannerImageFileId,
        String contentHtml,
        EventStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean deleted
    ) {
        return new EventJpaEntity(
            name,
            description,
            subtitle,
            thumbnailImageFileId,
            bannerImageFileId,
            contentHtml,
            status,
            startAt,
            endAt,
            deleted
        );
    }

    void applyChanges(
        String name,
        String description,
        String subtitle,
        Long thumbnailImageFileId,
        Long bannerImageFileId,
        String contentHtml,
        EventStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean deleted
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
        this.deleted = deleted;
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

    public Long getThumbnailImageFileId() {
        return this.thumbnailImageFileId;
    }

    public Long getBannerImageFileId() {
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
}
