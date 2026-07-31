package com.tastyhouse.domain.event.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.domain.event.domain.vo.EventId;

/**
 * 이벤트 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code EventJpaEntity} + {@code EventMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code EventRepository#save}를
 * 호출해야 한다.
 */
@Getter
public class Event {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private String name; // 이벤트명
    private String description; // 이벤트 설명
    private String subtitle; // 이벤트 부제목
    private Long thumbnailImageFileId; // 썸네일 이미지 파일 ID (FILE.id 참조)
    private Long bannerImageFileId; // 배너 이미지 파일 ID (FILE.id 참조)
    private String contentHtml; // 이벤트 본문 HTML
    private EventStatus status; // 이벤트 상태 (예: SCHEDULED, ACTIVE, ENDED)
    private LocalDateTime startAt; // 이벤트 시작 일시
    private LocalDateTime endAt; // 이벤트 종료 일시
    private boolean deleted; // 삭제 여부 (Soft Delete)
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private Event(
        Long id,
        String name,
        String description,
        String subtitle,
        Long thumbnailImageFileId,
        Long bannerImageFileId,
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

    /**
     * 신규 이벤트를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     */
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

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static Event reconstitute(
        Long id,
        String name,
        String description,
        String subtitle,
        Long thumbnailImageFileId,
        Long bannerImageFileId,
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
}
