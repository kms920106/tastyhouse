package com.tastyhouse.application.event.port.out;

import java.util.Optional;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface EventQueryPort {

    PageResult<EventListItemResult> findEventListItemsByStatus(EventStatus status, PageQuery pageQuery);

    Optional<EventDetailResult> findEventBannerById(EventId eventId);

    PageResult<EventAnnouncementResult> findAnnouncements(PageQuery pageQuery);
}
