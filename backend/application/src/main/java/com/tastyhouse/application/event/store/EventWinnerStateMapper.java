package com.tastyhouse.application.event.store;

import com.tastyhouse.application.event.port.out.write.EventWinnerState;
import com.tastyhouse.domain.event.model.EventWinner;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.shared.vo.PhoneNumber;

final class EventWinnerStateMapper {
    private EventWinnerStateMapper() {
    }

    static EventWinner toDomain(EventWinnerState state) {
        return EventWinner.reconstitute(
            state.id(),
            state.eventId() == null ? null : EventId.of(state.eventId()),
            state.rankNo(),
            state.winnerName(),
            state.phoneNumber() == null ? null : new PhoneNumber(state.phoneNumber()),
            state.announcedAt(),
            state.deleted()
        );
    }

    static EventWinnerState toState(EventWinner eventWinner) {
        return new EventWinnerState(
            eventWinner.getId(),
            eventWinner.getEventId() == null ? null : eventWinner.getEventId().value(),
            eventWinner.getRankNo(),
            eventWinner.getWinnerName(),
            eventWinner.getPhoneNumber() == null ? null : eventWinner.getPhoneNumber().value(),
            eventWinner.getAnnouncedAt(),
            eventWinner.isDeleted()
        );
    }
}
