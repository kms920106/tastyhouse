package com.tastyhouse.infrastructure.event.persistence;

import com.tastyhouse.application.event.port.out.write.EventAnnouncementState;

final class EventAnnouncementMapper {
    private EventAnnouncementMapper() {
    }

    static EventAnnouncementState toState(EventAnnouncementJpaEntity entity) {
        return new EventAnnouncementState(
            entity.getId(),
            entity.getEventId(),
            entity.getName(),
            entity.getContent(),
            entity.getAnnouncedAt()
        );
    }

    static EventAnnouncementJpaEntity toEntity(EventAnnouncementState state) {
        return EventAnnouncementJpaEntity.create(
            state.eventId(),
            state.name(),
            state.content(),
            state.announcedAt()
        );
    }

    static void applyChanges(EventAnnouncementJpaEntity entity, EventAnnouncementState state) {
        entity.applyChanges(
            state.name(),
            state.content(),
            state.announcedAt()
        );
    }
}
