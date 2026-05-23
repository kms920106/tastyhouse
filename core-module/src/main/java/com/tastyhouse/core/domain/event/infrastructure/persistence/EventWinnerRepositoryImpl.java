package com.tastyhouse.core.domain.event.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.event.domain.model.EventWinner;
import com.tastyhouse.core.domain.event.domain.repository.EventWinnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.domain.event.domain.model.QEventWinner.eventWinner;

@Repository
@RequiredArgsConstructor
public class EventWinnerRepositoryImpl implements EventWinnerRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<EventWinner> findByEventId(Long eventId) {
        return queryFactory
            .selectFrom(eventWinner)
            .where(eventWinner.eventId.eq(eventId))
            .fetch();
    }
}
