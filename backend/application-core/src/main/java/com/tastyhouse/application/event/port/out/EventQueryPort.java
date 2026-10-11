package com.tastyhouse.application.event.port.out;

import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface EventQueryPort {

    PageResult<EventListItemResult> findEventListItemsByStatus(String status, PageQuery pageQuery);

    Optional<EventDetailResult> findEventBannerById(Long eventId);

    PageResult<EventAnnouncementResult> findAnnouncements(PageQuery pageQuery);
}
