package com.tastyhouse.domain.event.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.event.vo.EventId;

public class EventAnnouncement {
    private final Long id;
    private final EventId eventId;
    private String name;
    private String content;
    private LocalDateTime announcedAt;

    private EventAnnouncement(
        Long id,
        EventId eventId,
        String name,
        String content,
        LocalDateTime announcedAt
    ) {
        this.id = id;
        this.eventId = eventId;
        this.name = name;
        this.content = content;
        this.announcedAt = announcedAt;
    }

    public static EventAnnouncement of(
        EventId eventId,
        String name,
        String content,
        LocalDateTime announcedAt
    ) {
        return new EventAnnouncement(null, eventId, name, content, announcedAt);
    }

    public static EventAnnouncement reconstitute(
        Long id,
        EventId eventId,
        String name,
        String content,
        LocalDateTime announcedAt
    ) {
        return new EventAnnouncement(id, eventId, name, content, announcedAt);
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
