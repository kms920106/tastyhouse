package com.tastyhouse.application.event.port.in;

import com.tastyhouse.application.event.port.out.EventManagementListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface EventManagementListQueryUseCase {

    PageResult<EventManagementListItemResult> getEvents(String name, String status, int page, int size);
}
