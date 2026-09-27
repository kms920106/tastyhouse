package com.tastyhouse.infrastructure.event.persistence;

import com.tastyhouse.domain.event.model.EventWinner;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.shared.vo.PhoneNumber;
import com.tastyhouse.infrastructure.shared.persistence.PhoneNumberEmbeddable;

final class EventWinnerMapper {
    private EventWinnerMapper() {
    }

    static EventWinner toDomain(EventWinnerJpaEntity entity) {
        return EventWinner.reconstitute(
            entity.getId(),
            entity.getEventId() == null ? null : EventId.of(entity.getEventId()),
            entity.getRankNo(),
            entity.getWinnerName(),
            toPhoneNumber(entity.getPhoneNumber()),
            entity.getAnnouncedAt(),
            entity.isDeleted()
        );
    }

    static EventWinnerJpaEntity toEntity(EventWinner eventWinner) {
        return EventWinnerJpaEntity.create(
            eventWinner.getEventId() == null ? null : eventWinner.getEventId().value(),
            eventWinner.getRankNo(),
            eventWinner.getWinnerName(),
            toPhoneNumberEmbeddable(eventWinner.getPhoneNumber()),
            eventWinner.getAnnouncedAt(),
            eventWinner.isDeleted()
        );
    }

    static void applyChanges(EventWinnerJpaEntity entity, EventWinner eventWinner) {
        entity.applyChanges(eventWinner.isDeleted());
    }

    private static PhoneNumber toPhoneNumber(PhoneNumberEmbeddable embeddable) {
        String phoneNumber = embeddable == null ? null : embeddable.value();
        return phoneNumber == null ? null : new PhoneNumber(phoneNumber);
    }

    private static PhoneNumberEmbeddable toPhoneNumberEmbeddable(PhoneNumber phoneNumber) {
        String value = phoneNumber == null ? null : phoneNumber.value();
        return value == null ? null : new PhoneNumberEmbeddable(value);
    }
}
