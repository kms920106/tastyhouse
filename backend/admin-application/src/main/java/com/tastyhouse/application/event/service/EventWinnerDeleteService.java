package com.tastyhouse.application.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.event.model.EventWinner;
import com.tastyhouse.application.event.port.in.EventWinnerDeleteCommand;
import com.tastyhouse.application.event.port.in.EventWinnerDeleteUseCase;
import com.tastyhouse.application.event.port.out.write.EventWinnerLoadPort;
import com.tastyhouse.application.event.port.out.write.EventWinnerSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class EventWinnerDeleteService implements EventWinnerDeleteUseCase {

    private final EventWinnerLoadPort eventWinnerLoadPort;
    private final EventWinnerSavePort eventWinnerSavePort;

    public EventWinnerDeleteService(EventWinnerLoadPort eventWinnerLoadPort, EventWinnerSavePort eventWinnerSavePort) {
        this.eventWinnerLoadPort = eventWinnerLoadPort;
        this.eventWinnerSavePort = eventWinnerSavePort;
    }

    @Override
    public void deleteWinner(EventWinnerDeleteCommand command) {
        EventWinner winner = eventWinnerLoadPort.findActiveById(command.winnerId())
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.EVENT_WINNER_NOT_FOUND));

        winner.delete();
        eventWinnerSavePort.save(winner);
    }
}
