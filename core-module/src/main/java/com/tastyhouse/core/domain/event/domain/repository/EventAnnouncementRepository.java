package com.tastyhouse.core.domain.event.domain.repository;

import com.tastyhouse.core.domain.event.domain.model.EventAnnouncement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventAnnouncementRepository {

    Page<EventAnnouncement> findAllOrderByAnnouncedAtDesc(Pageable pageable);
}
