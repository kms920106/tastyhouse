package com.tastyhouse.core.domain.event.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.event.domain.model.EventAnnouncement;
import com.tastyhouse.core.domain.event.domain.vo.EventId;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface EventAnnouncementRepository {

    PageResult<EventAnnouncement> findAllOrderByAnnouncedAtDesc(PageQuery pageQuery);

    Optional<EventAnnouncement> findByEventId(EventId eventId);

    boolean existsByEventId(EventId eventId);

    EventAnnouncement save(EventAnnouncement eventAnnouncement);
}
