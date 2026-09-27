package com.tastyhouse.infrastructure.event.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.event.port.out.write.EventWinnerState;
import com.tastyhouse.application.event.port.out.write.EventWinnerStatePort;

import static com.tastyhouse.infrastructure.event.persistence.QEventWinnerJpaEntity.eventWinnerJpaEntity;

@Repository
public class EventWinnerStatePortImpl implements EventWinnerStatePort {
    private final JPAQueryFactory queryFactory;
    private final EventWinnerJpaRepository eventWinnerJpaRepository;

    public EventWinnerStatePortImpl(JPAQueryFactory queryFactory, EventWinnerJpaRepository eventWinnerJpaRepository) {
        this.queryFactory = queryFactory;
        this.eventWinnerJpaRepository = eventWinnerJpaRepository;
    }

    @Override
    public Optional<EventWinnerState> findById(Long id) {
        EventWinnerJpaEntity entity = queryFactory
            .selectFrom(eventWinnerJpaEntity)
            .where(eventWinnerJpaEntity.id.eq(id), eventWinnerJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(EventWinnerMapper::toState);
    }

    @Override
    public EventWinnerState save(EventWinnerState state) {
        if (state.id() == null) {
            EventWinnerJpaEntity saved = eventWinnerJpaRepository.save(EventWinnerMapper.toEntity(state));
            return EventWinnerMapper.toState(saved);
        }

        EventWinnerJpaEntity entity = eventWinnerJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 이벤트 당첨자입니다: " + state.id()));
        EventWinnerMapper.applyChanges(entity, state);
        return EventWinnerMapper.toState(entity);
    }
}
