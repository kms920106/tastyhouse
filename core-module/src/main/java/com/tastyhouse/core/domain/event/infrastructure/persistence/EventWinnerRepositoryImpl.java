package com.tastyhouse.core.domain.event.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.event.domain.model.EventWinner;
import com.tastyhouse.core.domain.event.domain.repository.EventWinnerRepository;
import com.tastyhouse.core.domain.event.domain.vo.EventId;

import static com.tastyhouse.core.domain.event.domain.model.QEventWinner.eventWinner;

@Repository
@RequiredArgsConstructor
public class EventWinnerRepositoryImpl implements EventWinnerRepository {

    private final JPAQueryFactory queryFactory;
    private final EventWinnerJpaRepository eventWinnerJpaRepository;

    @Override
    public List<EventWinner> findByEventIdOrderByRankNo(EventId eventId) {
        return queryFactory
            .selectFrom(eventWinner)
            .where(eventWinner.eventId.eq(eventId.value()))
            .orderBy(eventWinner.rankNo.asc())
            .fetch();
    }

    @Override
    public Optional<EventWinner> findById(Long id) {
        return eventWinnerJpaRepository.findById(id);
    }

    @Override
    public EventWinner save(EventWinner newEventWinner) {
        return eventWinnerJpaRepository.save(newEventWinner);
    }

    @Override
    public void deleteById(Long id) {
        eventWinnerJpaRepository.deleteById(id);
    }
}
