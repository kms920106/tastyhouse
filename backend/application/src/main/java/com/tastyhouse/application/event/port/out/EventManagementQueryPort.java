package com.tastyhouse.application.event.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface EventManagementQueryPort {

    PageResult<EventManagementListItemResult> findAllEvents(EventSearchCondition condition, PageQuery pageQuery);

    Optional<EventManagementDetailResult> findEventDetailById(EventId eventId);

    List<EventWinnerResult> findWinnersByEventId(EventId eventId);

    Optional<EventAnnouncementResult> findAnnouncementByEventId(EventId eventId);
}
