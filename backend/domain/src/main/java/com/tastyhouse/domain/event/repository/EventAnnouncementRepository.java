package com.tastyhouse.domain.event.repository;

import java.util.Optional;

import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.vo.EventId;

public interface EventAnnouncementRepository {
    Optional<EventAnnouncement> findByEventId(EventId eventId);

    boolean existsByEventId(EventId eventId);

    EventAnnouncement save(EventAnnouncement eventAnnouncement);
}
