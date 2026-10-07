package com.tastyhouse.application.event.port.in;

import com.tastyhouse.application.event.port.out.EventManagementDetailResult;

public interface EventManagementDetailQueryUseCase {

    EventManagementDetailResult getEvent(Long id);
}
