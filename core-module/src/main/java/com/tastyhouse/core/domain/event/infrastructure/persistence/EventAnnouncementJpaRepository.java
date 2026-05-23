package com.tastyhouse.core.domain.event.infrastructure.persistence;

import com.tastyhouse.core.domain.event.domain.model.EventAnnouncement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventAnnouncementJpaRepository extends JpaRepository<EventAnnouncement, Long> {
}
