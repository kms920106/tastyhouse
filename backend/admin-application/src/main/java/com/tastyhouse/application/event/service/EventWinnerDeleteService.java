package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventWinner;
import com.tastyhouse.application.event.port.in.EventWinnerDeleteCommand;
import com.tastyhouse.application.event.port.in.EventWinnerDeleteUseCase;
import com.tastyhouse.application.event.port.out.write.EventWinnerPersistencePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class EventWinnerDeleteService implements EventWinnerDeleteUseCase {

    private final EventWinnerPersistencePort eventWinnerPersistencePort;

    public EventWinnerDeleteService(EventWinnerPersistencePort eventWinnerPersistencePort) {
        this.eventWinnerPersistencePort = eventWinnerPersistencePort;
    }

    @Override
    public void deleteWinner(EventWinnerDeleteCommand command) {
        EventWinner winner = eventWinnerPersistencePort.findById(command.winnerId())
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.EVENT_WINNER_NOT_FOUND));

        winner.delete();
        eventWinnerPersistencePort.save(winner);
    }
}
