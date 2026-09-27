package com.tastyhouse.application.event.port.out;

import java.util.Optional;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface EventQueryPort {

    PageResult<EventListItemResult> findEventListItemsByStatus(EventStatus status, PageQuery pageQuery);

    Optional<EventDetailResult> findEventBannerById(EventId eventId);

    PageResult<EventAnnouncementResult> findAnnouncements(PageQuery pageQuery);
}
