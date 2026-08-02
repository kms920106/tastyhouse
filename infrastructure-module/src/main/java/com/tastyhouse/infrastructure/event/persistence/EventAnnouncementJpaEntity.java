package com.tastyhouse.infrastructure.event.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.domain.event.domain.vo.EventId;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 이벤트 당첨자 발표 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code EventAnnouncement}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code EventAnnouncementMapper}가 수행한다.
 */
@Entity
@Table(
    name = "EVENT_ANNOUNCEMENT",
    indexes = {
        @Index(name = "idx_event_announcement_event_id", columnList = "event_id"),
        @Index(name = "idx_event_announcement_announced_at", columnList = "announced_at")
    }
)
public class EventAnnouncementJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Convert(converter = EventIdConverter.class)
    @Column(name = "event_id", nullable = false, unique = true)
    private EventId eventId; // 이벤트 ID (EVENT.id 참조)

    @Column(name = "name", nullable = false, length = 200)
    private String name; // 당첨자 발표 제목

    @Column(name = "content", nullable = false, length = 1000)
    private String content; // 당첨자 발표 내용

    @Column(name = "announced_at", nullable = false)
    private LocalDateTime announcedAt; // 당첨자 발표 일시

    protected EventAnnouncementJpaEntity() {
    }

    private EventAnnouncementJpaEntity(
        EventId eventId,
        String name,
        String content,
        LocalDateTime announcedAt
    ) {
        this.eventId = eventId;
        this.name = name;
        this.content = content;
        this.announcedAt = announcedAt;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code EventAnnouncementMapper#toEntity}에서만 호출한다.
     */
    static EventAnnouncementJpaEntity create(
        EventId eventId,
        String name,
        String content,
        LocalDateTime announcedAt
    ) {
        return new EventAnnouncementJpaEntity(eventId, name, content, announcedAt);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(
        String name,
        String content,
        LocalDateTime announcedAt
    ) {
        this.name = name;
        this.content = content;
        this.announcedAt = announcedAt;
    }

    public Long getId() {
        return this.id;
    }

    public EventId getEventId() {
        return this.eventId;
    }

    public String getName() {
        return this.name;
    }

    public String getContent() {
        return this.content;
    }

    public LocalDateTime getAnnouncedAt() {
        return this.announcedAt;
    }
}
