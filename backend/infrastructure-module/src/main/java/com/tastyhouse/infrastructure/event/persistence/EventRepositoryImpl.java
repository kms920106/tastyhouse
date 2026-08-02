package com.tastyhouse.infrastructure.event.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.event.model.Event;
import com.tastyhouse.domain.event.repository.EventRepository;
import com.tastyhouse.domain.event.vo.EventId;

import static com.tastyhouse.infrastructure.event.persistence.QEventJpaEntity.eventJpaEntity;

/**
 * 이벤트 write 어댑터.
 *
 * <p>command 경로의 단건 로드·저장만 담당한다. 표현 목적 read(목록·검색·상세)는 같은 모듈의
 * {@code EventQueryDao}로 이관했다(CQRS 분리).
 */
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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        EventJpaEntity entity = eventJpaRepository.findById(event.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 이벤트입니다: " + event.getId()));
        EventMapper.applyChanges(entity, event);
        return EventMapper.toDomain(entity);
    }
}
