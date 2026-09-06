package com.tastyhouse.infrastructure.event.persistence;

import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.infrastructure.shared.persistence.IdMapping;

final class EventAnnouncementMapper {
    private EventAnnouncementMapper() {
    }

    static EventAnnouncement toDomain(EventAnnouncementJpaEntity entity) {
        return EventAnnouncement.reconstitute(
            entity.getId(),
            IdMapping.vo(entity.getEventId(), EventId::of),
            entity.getName(),
            entity.getContent(),
            entity.getAnnouncedAt()
        );
    }

    static EventAnnouncementJpaEntity toEntity(EventAnnouncement domain) {
        return EventAnnouncementJpaEntity.create(
            IdMapping.raw(domain.getEventId(), EventId::value),
            domain.getName(),
            domain.getContent(),
            domain.getAnnouncedAt()
        );
    }

    static void applyChanges(EventAnnouncementJpaEntity entity, EventAnnouncement domain) {
        entity.applyChanges(
            domain.getName(),
            domain.getContent(),
            domain.getAnnouncedAt()
        );
    }
}
