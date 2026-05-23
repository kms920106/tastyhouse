package com.tastyhouse.core.entity.event;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
    name = "EVENT_ANNOUNCEMENT",
    indexes = {
        @Index(name = "idx_event_announcement_event_id", columnList = "event_id"),
        @Index(name = "idx_event_announcement_announced_at", columnList = "announced_at")
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventAnnouncement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "event_id", nullable = false, unique = true)
    private Long eventId; // 이벤트 ID (EVENT.id 참조)

    @Column(name = "name", nullable = false, length = 200)
    private String name; // 당첨자 발표 제목

    @Column(name = "content", nullable = false, length = 1000)
    private String content; // 당첨자 발표 내용

    @Column(name = "announced_at", nullable = false)
    private LocalDateTime announcedAt; // 당첨자 발표 일시

    private EventAnnouncement(
        Long eventId,
        String name,
        String content,
        LocalDateTime announcedAt
    ) {
        this.eventId = eventId;
        this.name = name;
        this.content = content;
        this.announcedAt = announcedAt;
    }

    public static EventAnnouncement of(
        Long eventId,
        String name,
        String content,
        LocalDateTime announcedAt
    ) {
        return new EventAnnouncement(
            eventId,
            name,
            content,
            announcedAt
        );
    }

    public void update(
        String name,
        String contentHtml,
        LocalDateTime announcedAt
    ) {
        this.name = name;
        this.content = contentHtml;
        this.announcedAt = announcedAt;
    }
}
