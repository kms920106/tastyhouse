package com.tastyhouse.application.event.port.out.write;

import java.util.Optional;

public interface EventAnnouncementStatePort {
    Optional<EventAnnouncementState> findByEventId(Long eventId);

    boolean existsByEventId(Long eventId);

    EventAnnouncementState save(EventAnnouncementState state);
}
