package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventWinner;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.application.event.port.in.EventWinnerCreateCommand;
import com.tastyhouse.application.event.port.in.EventWinnerCreateUseCase;
import com.tastyhouse.application.event.port.out.write.EventLoadPort;
import com.tastyhouse.application.event.port.out.write.EventWinnerSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class EventWinnerCreateService implements EventWinnerCreateUseCase {

    private final EventLoadPort eventLoadPort;
    private final EventWinnerSavePort eventWinnerSavePort;

    public EventWinnerCreateService(EventLoadPort eventLoadPort, EventWinnerSavePort eventWinnerSavePort) {
        this.eventLoadPort = eventLoadPort;
        this.eventWinnerSavePort = eventWinnerSavePort;
    }

    @Override
    public Long createWinner(EventWinnerCreateCommand command) {
        EventId eventId = EventId.of(command.eventId());
        verifyEventExists(eventId);

        EventWinner winner = EventWinner.of(eventId, command.rankNo(), command.winnerName(), command.phoneNumber(), command.announcedAt());
        EventWinner saved = eventWinnerSavePort.save(winner);
        return saved.getId();
    }

    private void verifyEventExists(EventId eventId) {
        eventLoadPort.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.EVENT_NOT_FOUND));
    }
}
