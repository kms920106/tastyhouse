package com.tastyhouse.application.event.port.in;

import com.tastyhouse.application.event.port.out.EventDetailResult;

public interface EventDetailQueryUseCase {

    EventDetailResult getEventDetail(Long eventId);
}
