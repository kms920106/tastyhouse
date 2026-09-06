package com.tastyhouse.infrastructure.event.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

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
    private Long id;

    @Column(name = "event_id", nullable = false, unique = true)
    private Long eventId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "announced_at", nullable = false)
    private LocalDateTime announcedAt;

    protected EventAnnouncementJpaEntity() {
    }

    private EventAnnouncementJpaEntity(
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

    static EventAnnouncementJpaEntity create(
        Long eventId,
        String name,
        String content,
        LocalDateTime announcedAt
    ) {
        return new EventAnnouncementJpaEntity(eventId, name, content, announcedAt);
    }

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

    public Long getEventId() {
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
