package com.tastyhouse.application.event.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import java.util.List;

import com.tastyhouse.application.event.port.out.EventAnnouncementResult;
import com.tastyhouse.application.event.port.out.EventManagementDetailResult;
import com.tastyhouse.application.event.port.out.EventManagementListItemResult;
import com.tastyhouse.application.event.port.out.EventWinnerResult;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface EventManagementQueryUseCase {

    PageResult<EventManagementListItemResult> getEvents(String name, String status, int page, int size);

    EventManagementDetailResult getEvent(Long id);

    EventAnnouncementResult getAnnouncement(Long id);

    List<EventWinnerResult> getWinners(Long id);
}
