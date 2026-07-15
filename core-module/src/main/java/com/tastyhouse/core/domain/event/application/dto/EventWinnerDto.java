package com.tastyhouse.core.domain.event.application.dto;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.event.domain.model.EventWinner;

public record EventWinnerDto(
    Long id,
    Long eventId,
    Integer rankNo,
    String winnerName,
    String phoneNumber,
    LocalDateTime announcedAt
) {

    public static EventWinnerDto from(EventWinner winner) {
        return new EventWinnerDto(
            winner.getId(),
            winner.getEventId(),
            winner.getRankNo(),
            winner.getWinnerName(),
            winner.getPhoneNumber().getValue(),
            winner.getAnnouncedAt()
        );
    }
}
