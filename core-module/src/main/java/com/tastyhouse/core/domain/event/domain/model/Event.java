package com.tastyhouse.core.domain.event.domain.model;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.event.domain.vo.EventId;
import com.tastyhouse.core.shared.entity.BaseEntity;

@Getter
@Entity
@Table(
    name = "EVENT",
    indexes = {
        @Index(name = "idx_event_active", columnList = "is_deleted, status"),
        @Index(name = "idx_event_period", columnList = "start_at, end_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "name", nullable = false, length = 200)
    private String name; // 이벤트명

    @Column(name = "description", length = 1000)
    private String description; // 이벤트 설명

    @Column(name = "subtitle", length = 200)
    private String subtitle; // 이벤트 부제목

    @Column(name = "thumbnail_image_file_id")
    private Long thumbnailImageFileId; // 썸네일 이미지 파일 ID (FILE.id 참조)

    @Column(name = "banner_image_file_id")
    private Long bannerImageFileId; // 배너 이미지 파일 ID (FILE.id 참조)

    @Column(name = "content_html", columnDefinition = "TEXT")
    private String contentHtml; // 이벤트 본문 HTML

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private EventStatus status; // 이벤트 상태 (예: SCHEDULED, ACTIVE, ENDED)

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt; // 이벤트 시작 일시

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt; // 이벤트 종료 일시

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false; // 삭제 여부 (Soft Delete)

    private Event(
        String name,
        String description,
        String subtitle,
        Long thumbnailImageFileId,
        Long bannerImageFileId,
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

    public static Event of(
        String name,
        String description,
        String subtitle,
        Long thumbnailImageFileId,
        Long bannerImageFileId,
        String contentHtml,
        EventStatus status,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        return new Event(
            name,
            description,
            subtitle,
            thumbnailImageFileId,
            bannerImageFileId,
            contentHtml,
            status,
            startAt,
            endAt
        );
    }

    public void update(
        String name,
        String description,
        String subtitle,
        Long thumbnailImageFileId,
        Long bannerImageFileId,
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

    public EventId getEventId() {
        return EventId.of(this.id);
    }
}
