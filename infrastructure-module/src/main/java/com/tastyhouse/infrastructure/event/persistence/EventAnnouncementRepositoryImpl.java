package com.tastyhouse.infrastructure.event.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.event.domain.model.EventAnnouncement;
import com.tastyhouse.core.domain.event.domain.repository.EventAnnouncementRepository;
import com.tastyhouse.core.domain.event.domain.vo.EventId;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.infrastructure.event.persistence.QEventAnnouncementJpaEntity.eventAnnouncementJpaEntity;

@Repository
@RequiredArgsConstructor
public class EventAnnouncementRepositoryImpl implements EventAnnouncementRepository {

    private final JPAQueryFactory queryFactory;
    private final EventAnnouncementJpaRepository eventAnnouncementJpaRepository;

    @Override
    public PageResult<EventAnnouncement> findAllOrderByAnnouncedAtDesc(PageQuery pageQuery) {
        List<EventAnnouncement> content = queryFactory
            .selectFrom(eventAnnouncementJpaEntity)
            .orderBy(eventAnnouncementJpaEntity.announcedAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(EventAnnouncementMapper::toDomain)
            .toList();

        JPAQuery<Long> countQuery = queryFactory
            .select(eventAnnouncementJpaEntity.count())
            .from(eventAnnouncementJpaEntity);

        Long total = countQuery.fetchOne();
        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        EventAnnouncementJpaEntity entity = eventAnnouncementJpaRepository.findById(eventAnnouncement.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 이벤트 발표입니다: " + eventAnnouncement.getId()));
        EventAnnouncementMapper.applyChanges(entity, eventAnnouncement);
        return EventAnnouncementMapper.toDomain(entity);
    }
}
