package com.tastyhouse.application.event.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.event.port.out.EventAnnouncementResult;
import com.tastyhouse.application.event.port.out.EventDetailResult;
import com.tastyhouse.application.event.port.out.EventListItemResult;

@WebApp
public interface EventQueryUseCase {

    PageResult<EventListItemResult> getEventList(String status, int page, int size);

    EventDetailResult getEventDetail(Long eventId);

    PageResult<EventAnnouncementResult> getEventAnnouncementList(int page, int size);
}
