package com.tastyhouse.infrastructure.event.persistence;

import com.tastyhouse.domain.event.model.EventWinner;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class EventWinnerMapper {
    private EventWinnerMapper() {
    }

    static EventWinner toDomain(EventWinnerJpaEntity entity) {
        return EventWinner.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getEventId(), EventId::of),
            entity.getRankNo(),
            entity.getWinnerName(),
            entity.getPhoneNumber(),
            entity.getAnnouncedAt(),
            entity.isDeleted()
        );
    }

    static EventWinnerJpaEntity toEntity(EventWinner domain) {
        return EventWinnerJpaEntity.create(
            IdMapping.raw(domain.getEventId(), EventId::value),
            domain.getRankNo(),
            domain.getWinnerName(),
            domain.getPhoneNumber(),
            domain.getAnnouncedAt(),
            domain.isDeleted()
        );
    }

    static void applyChanges(EventWinnerJpaEntity entity, EventWinner domain) {
        entity.applyChanges(domain.isDeleted());
    }
}
