package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.model.EventWinner;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.application.event.port.in.EventWinnerCreateCommand;
import com.tastyhouse.application.event.port.in.EventWinnerCreateUseCase;
import com.tastyhouse.application.event.port.out.write.EventPersistencePort;
import com.tastyhouse.application.event.port.out.write.EventWinnerPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class EventWinnerCreateService implements EventWinnerCreateUseCase {

    private final EventPersistencePort eventPersistencePort;
    private final EventWinnerPersistencePort eventWinnerPersistencePort;

    public EventWinnerCreateService(EventPersistencePort eventPersistencePort, EventWinnerPersistencePort eventWinnerPersistencePort) {
        this.eventPersistencePort = eventPersistencePort;
        this.eventWinnerPersistencePort = eventWinnerPersistencePort;
    }

    @Override
    public Long createWinner(EventWinnerCreateCommand command) {
        EventId eventId = EventId.of(command.eventId());
        findEventOrThrow(eventId);

        EventWinner winner = EventWinner.of(eventId, command.rankNo(), command.winnerName(), command.phoneNumber(), command.announcedAt());
        EventWinner saved = eventWinnerPersistencePort.save(winner);
        return saved.getId();
    }

    private Event findEventOrThrow(EventId eventId) {
        return eventPersistencePort.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.EVENT_NOT_FOUND));
    }
}
