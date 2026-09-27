package com.tastyhouse.infrastructure.event.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.event.port.out.write.EventState;
import com.tastyhouse.application.event.port.out.write.EventStatePort;

import static com.tastyhouse.infrastructure.event.persistence.QEventJpaEntity.eventJpaEntity;

@Repository
public class EventStatePortImpl implements EventStatePort {
    private final JPAQueryFactory queryFactory;
    private final EventJpaRepository eventJpaRepository;

    public EventStatePortImpl(JPAQueryFactory queryFactory, EventJpaRepository eventJpaRepository) {
        this.queryFactory = queryFactory;
        this.eventJpaRepository = eventJpaRepository;
    }

    @Override
    public Optional<EventState> findById(Long eventId) {
        EventJpaEntity entity = queryFactory
            .selectFrom(eventJpaEntity)
            .where(eventJpaEntity.id.eq(eventId), eventJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(EventMapper::toState);
    }

    @Override
    public EventState save(EventState state) {
        if (state.id() == null) {
            EventJpaEntity saved = eventJpaRepository.save(EventMapper.toEntity(state));
            return EventMapper.toState(saved);
        }

        EventJpaEntity entity = eventJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 이벤트입니다: " + state.id()));
        EventMapper.applyChanges(entity, state);
        return EventMapper.toState(entity);
    }
}
