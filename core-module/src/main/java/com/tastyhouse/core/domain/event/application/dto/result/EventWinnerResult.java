package com.tastyhouse.core.domain.event.application.dto.result;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.event.domain.model.EventWinner;

public record EventWinnerResult(
    Long id,
    Long eventId,
    Integer rankNo,
    String winnerName,
    String phoneNumber,
    LocalDateTime announcedAt
) {

    public static EventWinnerResult from(EventWinner winner) {
        return new EventWinnerResult(
            winner.getId(),
            winner.getEventId(),
            winner.getRankNo(),
            winner.getWinnerName(),
            winner.getPhoneNumber().getValue(),
            winner.getAnnouncedAt()
        );
    }
}
