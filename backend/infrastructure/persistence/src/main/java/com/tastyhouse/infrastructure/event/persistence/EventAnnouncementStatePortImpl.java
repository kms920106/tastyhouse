package com.tastyhouse.infrastructure.event.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.event.port.out.write.EventAnnouncementState;
import com.tastyhouse.application.event.port.out.write.EventAnnouncementStatePort;

import static com.tastyhouse.infrastructure.event.persistence.QEventAnnouncementJpaEntity.eventAnnouncementJpaEntity;

@Repository
public class EventAnnouncementStatePortImpl implements EventAnnouncementStatePort {
    private final JPAQueryFactory queryFactory;
    private final EventAnnouncementJpaRepository eventAnnouncementJpaRepository;

    public EventAnnouncementStatePortImpl(JPAQueryFactory queryFactory, EventAnnouncementJpaRepository eventAnnouncementJpaRepository) {
        this.queryFactory = queryFactory;
        this.eventAnnouncementJpaRepository = eventAnnouncementJpaRepository;
    }

    @Override
    public Optional<EventAnnouncementState> findByEventId(Long eventId) {
        EventAnnouncementJpaEntity entity = queryFactory
            .selectFrom(eventAnnouncementJpaEntity)
            .where(eventAnnouncementJpaEntity.eventId.eq(eventId))
            .fetchOne();
        return Optional.ofNullable(entity).map(EventAnnouncementMapper::toState);
    }

    @Override
    public boolean existsByEventId(Long eventId) {
        Integer result = queryFactory
            .selectOne()
            .from(eventAnnouncementJpaEntity)
            .where(eventAnnouncementJpaEntity.eventId.eq(eventId))
            .fetchFirst();
        return result != null;
    }

    @Override
    public EventAnnouncementState save(EventAnnouncementState state) {
        if (state.id() == null) {
            EventAnnouncementJpaEntity saved = eventAnnouncementJpaRepository.save(EventAnnouncementMapper.toEntity(state));
            return EventAnnouncementMapper.toState(saved);
        }

        EventAnnouncementJpaEntity entity = eventAnnouncementJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 이벤트 발표입니다: " + state.id()));
        EventAnnouncementMapper.applyChanges(entity, state);
        return EventAnnouncementMapper.toState(entity);
    }
}
