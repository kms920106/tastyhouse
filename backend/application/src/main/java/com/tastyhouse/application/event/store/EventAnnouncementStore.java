package com.tastyhouse.application.event.store;

import java.util.Optional;

import com.tastyhouse.application.event.port.out.write.EventAnnouncementStatePort;
import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.vo.EventId;

public class EventAnnouncementStore implements EventAnnouncementRepository {
    private final EventAnnouncementStatePort eventAnnouncementStatePort;

    public EventAnnouncementStore(EventAnnouncementStatePort eventAnnouncementStatePort) {
        this.eventAnnouncementStatePort = eventAnnouncementStatePort;
    }

    @Override
    public Optional<EventAnnouncement> findByEventId(EventId eventId) {
        return eventAnnouncementStatePort.findByEventId(eventId.value()).map(EventAnnouncementStateMapper::toDomain);
    }

    @Override
    public boolean existsByEventId(EventId eventId) {
        return eventAnnouncementStatePort.existsByEventId(eventId.value());
    }

    @Override
    public EventAnnouncement save(EventAnnouncement eventAnnouncement) {
        return EventAnnouncementStateMapper.toDomain(
            eventAnnouncementStatePort.save(EventAnnouncementStateMapper.toState(eventAnnouncement)));
    }
}
