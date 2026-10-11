package com.tastyhouse.application.event.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface EventManagementQueryPort {

    PageResult<EventManagementListItemResult> findAllEvents(EventSearchCondition condition, PageQuery pageQuery);

    Optional<EventManagementDetailResult> findEventDetailById(Long eventId);

    List<EventWinnerResult> findWinnersByEventId(Long eventId);

    Optional<EventAnnouncementResult> findAnnouncementByEventId(Long eventId);
}
