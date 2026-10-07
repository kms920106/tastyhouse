package com.tastyhouse.application.event.port.in;

import com.tastyhouse.application.event.port.out.EventListItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface EventListQueryUseCase {

    PageResult<EventListItemResult> getEventList(String status, int page, int size);
}
