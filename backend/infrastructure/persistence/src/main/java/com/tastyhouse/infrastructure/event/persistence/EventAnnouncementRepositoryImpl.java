package com.tastyhouse.infrastructure.event.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.event.model.EventAnnouncement;
import com.tastyhouse.domain.event.repository.EventAnnouncementRepository;
import com.tastyhouse.domain.event.vo.EventId;

import static com.tastyhouse.infrastructure.event.persistence.QEventAnnouncementJpaEntity.eventAnnouncementJpaEntity;

@Repository
public class EventAnnouncementRepositoryImpl implements EventAnnouncementRepository {
    private final JPAQueryFactory queryFactory;
    private final EventAnnouncementJpaRepository eventAnnouncementJpaRepository;

    public EventAnnouncementRepositoryImpl(JPAQueryFactory queryFactory, EventAnnouncementJpaRepository eventAnnouncementJpaRepository) {
        this.queryFactory = queryFactory;
        this.eventAnnouncementJpaRepository = eventAnnouncementJpaRepository;
    }

    @Override
    public Optional<EventAnnouncement> findByEventId(EventId eventId) {
        EventAnnouncementJpaEntity entity = queryFactory
            .selectFrom(eventAnnouncementJpaEntity)
            .where(eventAnnouncementJpaEntity.eventId.eq(eventId.value()))
            .fetchOne();
        return Optional.ofNullable(entity).map(EventAnnouncementMapper::toDomain);
    }

    @Override
    public boolean existsByEventId(EventId eventId) {
        Integer result = queryFactory
            .selectOne()
            .from(eventAnnouncementJpaEntity)
            .where(eventAnnouncementJpaEntity.eventId.eq(eventId.value()))
            .fetchFirst();
        return result != null;
    }

    @Override
    public EventAnnouncement save(EventAnnouncement eventAnnouncement) {
        if (eventAnnouncement.getId() == null) {
            EventAnnouncementJpaEntity saved = eventAnnouncementJpaRepository.save(EventAnnouncementMapper.toEntity(eventAnnouncement));
            return EventAnnouncementMapper.toDomain(saved);
        }

        EventAnnouncementJpaEntity entity = eventAnnouncementJpaRepository.findById(eventAnnouncement.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 이벤트 발표입니다: " + eventAnnouncement.getId()));
        EventAnnouncementMapper.applyChanges(entity, eventAnnouncement);
        return EventAnnouncementMapper.toDomain(entity);
    }
}
