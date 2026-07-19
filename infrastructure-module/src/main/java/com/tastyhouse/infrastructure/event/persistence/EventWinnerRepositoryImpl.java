package com.tastyhouse.infrastructure.event.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.event.domain.model.EventWinner;
import com.tastyhouse.core.domain.event.domain.repository.EventWinnerRepository;
import com.tastyhouse.core.domain.event.domain.vo.EventId;

import static com.tastyhouse.infrastructure.event.persistence.QEventWinnerJpaEntity.eventWinnerJpaEntity;

@Repository
@RequiredArgsConstructor
public class EventWinnerRepositoryImpl implements EventWinnerRepository {

    private final JPAQueryFactory queryFactory;
    private final EventWinnerJpaRepository eventWinnerJpaRepository;

    @Override
    public List<EventWinner> findByEventIdOrderByRankNo(EventId eventId) {
        return queryFactory
            .selectFrom(eventWinnerJpaEntity)
            .where(eventWinnerJpaEntity.eventId.eq(eventId.value()), eventWinnerJpaEntity.deleted.isFalse())
            .orderBy(eventWinnerJpaEntity.rankNo.asc())
            .fetch()
            .stream()
            .map(EventWinnerMapper::toDomain)
            .toList();
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

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        EventWinnerJpaEntity entity = eventWinnerJpaRepository.findById(eventWinner.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 이벤트 당첨자입니다: " + eventWinner.getId()));
        EventWinnerMapper.applyChanges(entity, eventWinner);
        return EventWinnerMapper.toDomain(entity);
    }
}
