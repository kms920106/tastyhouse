package com.tastyhouse.infrastructure.event.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.repository.EventRepository;
import com.tastyhouse.domain.event.vo.EventId;

import static com.tastyhouse.infrastructure.event.persistence.QEventJpaEntity.eventJpaEntity;

@Repository
public class EventRepositoryImpl implements EventRepository {
    private final JPAQueryFactory queryFactory;
    private final EventJpaRepository eventJpaRepository;

    public EventRepositoryImpl(JPAQueryFactory queryFactory, EventJpaRepository eventJpaRepository) {
        this.queryFactory = queryFactory;
        this.eventJpaRepository = eventJpaRepository;
    }

    @Override
    public Optional<Event> findById(EventId eventId) {
        EventJpaEntity entity = queryFactory
            .selectFrom(eventJpaEntity)
            .where(eventJpaEntity.id.eq(eventId.value()), eventJpaEntity.deleted.isFalse())
            .fetchOne();
        return Optional.ofNullable(entity).map(EventMapper::toDomain);
    }

    @Override
    public Event save(Event event) {
        if (event.getId() == null) {
            EventJpaEntity saved = eventJpaRepository.save(EventMapper.toEntity(event));
            return EventMapper.toDomain(saved);
        }

        EventJpaEntity entity = eventJpaRepository.findById(event.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 이벤트입니다: " + event.getId()));
        EventMapper.applyChanges(entity, event);
        return EventMapper.toDomain(entity);
    }
}
