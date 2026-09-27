package com.tastyhouse.application.event.store;

import com.tastyhouse.application.event.port.out.write.EventAnnouncementState;
import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.vo.EventId;

final class EventAnnouncementStateMapper {
    private EventAnnouncementStateMapper() {
    }

    static EventAnnouncement toDomain(EventAnnouncementState state) {
        return EventAnnouncement.reconstitute(
            state.id(),
            state.eventId() == null ? null : EventId.of(state.eventId()),
            state.name(),
            state.content(),
            state.announcedAt()
        );
    }

    static EventAnnouncementState toState(EventAnnouncement eventAnnouncement) {
        return new EventAnnouncementState(
            eventAnnouncement.getId(),
            eventAnnouncement.getEventId() == null ? null : eventAnnouncement.getEventId().value(),
            eventAnnouncement.getName(),
            eventAnnouncement.getContent(),
            eventAnnouncement.getAnnouncedAt()
        );
    }
}
