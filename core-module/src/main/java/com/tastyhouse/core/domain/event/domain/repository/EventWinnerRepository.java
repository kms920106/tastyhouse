package com.tastyhouse.core.domain.event.domain.repository;

import com.tastyhouse.core.domain.event.domain.model.EventWinner;

import java.util.List;

public interface EventWinnerRepository {

    List<EventWinner> findByEventId(Long eventId);
}
