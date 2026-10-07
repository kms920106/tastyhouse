package com.tastyhouse.application.event.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.application.event.port.in.EventManagementWinnerListQueryUseCase;
import com.tastyhouse.application.event.port.out.EventManagementQueryPort;
import com.tastyhouse.application.event.port.out.EventWinnerResult;

@Service
@Transactional(readOnly = true)
class EventManagementWinnerListQueryService implements EventManagementWinnerListQueryUseCase {

    private final EventManagementQueryPort eventManagementQueryPort;

    public EventManagementWinnerListQueryService(EventManagementQueryPort eventManagementQueryPort) {
        this.eventManagementQueryPort = eventManagementQueryPort;
    }

    @Override
    public List<EventWinnerResult> getWinners(Long id) {
        return eventManagementQueryPort.findWinnersByEventId(EventId.of(id).value());
    }
}
