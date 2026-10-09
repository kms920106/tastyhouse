package com.tastyhouse.infrastructure.persistence.event.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.event.model.EventWinner;
import com.tastyhouse.application.event.port.out.write.EventWinnerLoadPort;
import com.tastyhouse.application.event.port.out.write.EventWinnerSavePort;

import static com.tastyhouse.infrastructure.persistence.event.persistence.QEventWinnerJpaEntity.eventWinnerJpaEntity;

@Repository
class EventWinnerPersistenceAdapter implements EventWinnerLoadPort, EventWinnerSavePort {

    private final JPAQueryFactory queryFactory;
    private final EventWinnerJpaRepository eventWinnerJpaRepository;

    public EventWinnerPersistenceAdapter(JPAQueryFactory queryFactory, EventWinnerJpaRepository eventWinnerJpaRepository) {
        this.queryFactory = queryFactory;
        this.eventWinnerJpaRepository = eventWinnerJpaRepository;
    }

    @Override
    public Optional<EventWinner> findById(Long id) {
        EventWinnerJpaEntity entity = queryFactory
            .selectFrom(eventWinnerJpaEntity)
            .where(eventWinnerJpaEntity.id.eq(id), eventWinnerJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(EventWinnerMapper::toDomain);
    }

    @Override
    public EventWinner save(EventWinner eventWinner) {
        if (eventWinner.getId() == null) {
            EventWinnerJpaEntity saved = eventWinnerJpaRepository.save(EventWinnerMapper.toEntity(eventWinner));
            return EventWinnerMapper.toDomain(saved);
        }

        EventWinnerJpaEntity entity = eventWinnerJpaRepository.findById(eventWinner.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 이벤트 당첨자입니다: " + eventWinner.getId()));
        EventWinnerMapper.applyChanges(entity, eventWinner);
        return EventWinnerMapper.toDomain(entity);
    }
}
