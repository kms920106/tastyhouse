package com.tastyhouse.application.event.port.in;

import java.util.List;

import com.tastyhouse.application.event.port.out.EventWinnerResult;

public interface EventManagementWinnerListQueryUseCase {

    List<EventWinnerResult> getWinners(Long id);
}
