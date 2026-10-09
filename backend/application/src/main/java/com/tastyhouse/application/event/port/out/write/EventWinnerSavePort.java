package com.tastyhouse.application.event.port.out.write;

import com.tastyhouse.domain.event.model.EventWinner;

public interface EventWinnerSavePort {

    EventWinner save(EventWinner eventWinner);
}
