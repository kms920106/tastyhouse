package com.tastyhouse.domain.event.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.shared.vo.PhoneNumber;

public class EventWinner {
    private final Long id;
    private final EventId eventId;
    private final Integer rankNo;
    private final String winnerName;
    private final PhoneNumber phoneNumber;
    private final LocalDateTime announcedAt;
    private boolean deleted;

    private EventWinner(
        Long id,
        EventId eventId,
        Integer rankNo,
        String winnerName,
        PhoneNumber phoneNumber,
        LocalDateTime announcedAt,
        boolean deleted
    ) {
        this.id = id;
        this.eventId = eventId;
        this.rankNo = rankNo;
        this.winnerName = winnerName;
        this.phoneNumber = phoneNumber;
        this.announcedAt = announcedAt;
        this.deleted = deleted;
    }

    public static EventWinner of(
        EventId eventId,
        Integer rankNo,
        String winnerName,
        String phoneNumber,
        LocalDateTime announcedAt
    ) {
        return new EventWinner(
            null,
            eventId,
            rankNo,
            winnerName,
            new PhoneNumber(phoneNumber),
            announcedAt,
            false
        );
    }

    public static EventWinner reconstitute(
        Long id,
        EventId eventId,
        Integer rankNo,
        String winnerName,
        PhoneNumber phoneNumber,
        LocalDateTime announcedAt,
        boolean deleted
    ) {
        return new EventWinner(id, eventId, rankNo, winnerName, phoneNumber, announcedAt, deleted);
    }

    public void delete() {
        this.deleted = true;
    }

    public Long getId() {
        return this.id;
    }

    public EventId getEventId() {
        return this.eventId;
    }

    public Integer getRankNo() {
        return this.rankNo;
    }

    public String getWinnerName() {
        return this.winnerName;
    }

    public PhoneNumber getPhoneNumber() {
        return this.phoneNumber;
    }

    public LocalDateTime getAnnouncedAt() {
        return this.announcedAt;
    }

    public boolean isDeleted() {
        return this.deleted;
    }
}
