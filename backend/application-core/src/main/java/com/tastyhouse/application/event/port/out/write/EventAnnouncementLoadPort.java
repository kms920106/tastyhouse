package com.tastyhouse.application.event.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.vo.EventId;

public interface EventAnnouncementLoadPort {

    Optional<EventAnnouncement> findByEventId(EventId eventId);

    boolean existsByEventId(EventId eventId);
}
