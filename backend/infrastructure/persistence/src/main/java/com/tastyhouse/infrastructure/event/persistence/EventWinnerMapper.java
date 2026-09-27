package com.tastyhouse.infrastructure.event.persistence;

import com.tastyhouse.application.event.port.out.write.EventWinnerState;
import com.tastyhouse.infrastructure.shared.persistence.PhoneNumberEmbeddable;

final class EventWinnerMapper {
    private EventWinnerMapper() {
    }

    static EventWinnerState toState(EventWinnerJpaEntity entity) {
        return new EventWinnerState(
            entity.getId(),
            entity.getEventId(),
            entity.getRankNo(),
            entity.getWinnerName(),
            entity.getPhoneNumber() == null ? null : entity.getPhoneNumber().value(),
            entity.getAnnouncedAt(),
            entity.isDeleted()
        );
    }

    static EventWinnerJpaEntity toEntity(EventWinnerState state) {
        return EventWinnerJpaEntity.create(
            state.eventId(),
            state.rankNo(),
            state.winnerName(),
            state.phoneNumber() == null ? null : new PhoneNumberEmbeddable(state.phoneNumber()),
            state.announcedAt(),
            state.deleted()
        );
    }

    static void applyChanges(EventWinnerJpaEntity entity, EventWinnerState state) {
        entity.applyChanges(state.deleted());
    }
}
