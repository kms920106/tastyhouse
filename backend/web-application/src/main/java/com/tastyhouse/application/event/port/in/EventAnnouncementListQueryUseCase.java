package com.tastyhouse.application.event.port.in;

import com.tastyhouse.application.event.port.out.EventAnnouncementResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface EventAnnouncementListQueryUseCase {

    PageResult<EventAnnouncementResult> getEventAnnouncementList(int page, int size);
}
