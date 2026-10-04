package com.tastyhouse.infrastructure.persistence.event.persistence;

import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.vo.EventId;

final class EventAnnouncementMapper {

    private EventAnnouncementMapper() {
    }

    static EventAnnouncement toDomain(EventAnnouncementJpaEntity entity) {
        return EventAnnouncement.reconstitute(
            entity.getId(),
            entity.getEventId() == null ? null : EventId.of(entity.getEventId()),
            entity.getName(),
            entity.getContent(),
            entity.getAnnouncedAt()
        );
    }

    static EventAnnouncementJpaEntity toEntity(EventAnnouncement eventAnnouncement) {
        return EventAnnouncementJpaEntity.create(
            eventAnnouncement.getEventId() == null ? null : eventAnnouncement.getEventId().value(),
            eventAnnouncement.getName(),
            eventAnnouncement.getContent(),
            eventAnnouncement.getAnnouncedAt()
        );
    }

    static void applyChanges(EventAnnouncementJpaEntity entity, EventAnnouncement eventAnnouncement) {
        entity.applyChanges(
            eventAnnouncement.getName(),
            eventAnnouncement.getContent(),
            eventAnnouncement.getAnnouncedAt()
        );
    }
}
