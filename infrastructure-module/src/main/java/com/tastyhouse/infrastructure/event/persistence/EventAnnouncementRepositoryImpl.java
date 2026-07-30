package com.tastyhouse.infrastructure.event.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.event.domain.model.EventAnnouncement;
import com.tastyhouse.core.domain.event.domain.repository.EventAnnouncementRepository;
import com.tastyhouse.core.domain.event.domain.vo.EventId;

import static com.tastyhouse.infrastructure.event.persistence.QEventAnnouncementJpaEntity.eventAnnouncementJpaEntity;

/**
 * 이벤트 당첨자 발표 write 어댑터.
 *
 * <p>command 경로의 단건 로드·중복 검증·저장만 담당한다. 발표 목록 조회(표현 목적 read)는 같은 모듈의
 * {@code EventQueryDao}로 이관했다(CQRS 분리).
 */
@Repository
@RequiredArgsConstructor
public class EventAnnouncementRepositoryImpl implements EventAnnouncementRepository {

    private final JPAQueryFactory queryFactory;
    private final EventAnnouncementJpaRepository eventAnnouncementJpaRepository;

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
