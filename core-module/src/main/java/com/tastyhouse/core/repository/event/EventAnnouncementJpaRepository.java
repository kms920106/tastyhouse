package com.tastyhouse.core.repository.event;

import com.tastyhouse.core.entity.event.EventAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAnnouncementJpaRepository extends JpaRepository<EventAnnouncement, Long> {
}
