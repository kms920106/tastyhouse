package com.tastyhouse.infrastructure.event.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.event.domain.model.Event;
import com.tastyhouse.core.domain.event.domain.model.EventStatus;
import com.tastyhouse.core.domain.event.domain.repository.EventRepository;
import com.tastyhouse.core.domain.event.domain.vo.EventId;
import com.tastyhouse.core.domain.event.application.dto.EventSearchCondition;
import com.tastyhouse.core.domain.event.application.dto.result.EventDetailResult;
import com.tastyhouse.core.domain.event.application.dto.result.EventListItemResult;
import com.tastyhouse.core.domain.event.application.dto.result.EventManagementListItemResult;
import com.tastyhouse.core.domain.event.application.dto.result.QEventDetailResult;
import com.tastyhouse.core.domain.event.application.dto.result.QEventListItemResult;
import com.tastyhouse.core.domain.event.application.dto.result.QEventManagementListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.infrastructure.event.persistence.QEventJpaEntity.eventJpaEntity;

@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {

    private final JPAQueryFactory queryFactory;
    private final EventJpaRepository eventJpaRepository;

    @Override
    public PageResult<EventListItemResult> findEventListItemsByStatus(EventStatus status, PageQuery pageQuery) {
        List<EventListItemResult> content = queryFactory
            .select(new QEventListItemResult(
                eventJpaEntity.id,
                eventJpaEntity.name,
                uploadedFile.filePath,
                eventJpaEntity.startAt,
                eventJpaEntity.endAt
            ))
            .from(eventJpaEntity)
            .leftJoin(uploadedFile).on(eventJpaEntity.thumbnailImageFileId.eq(uploadedFile.id))
            .where(eventJpaEntity.status.eq(status))
            .orderBy(eventJpaEntity.startAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(eventJpaEntity.count())
            .from(eventJpaEntity)
            .where(eventJpaEntity.status.eq(status));

        Long total = countQuery.fetchOne();
        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<EventDetailResult> findEventDetailById(EventId eventId) {
        EventDetailResult result = queryFactory
            .select(new QEventDetailResult(
                uploadedFile.filePath
            ))
            .from(eventJpaEntity)
            .leftJoin(uploadedFile).on(eventJpaEntity.bannerImageFileId.eq(uploadedFile.id))
            .where(eventJpaEntity.id.eq(eventId.value()))
            .fetchOne();

        return Optional.ofNullable(result);
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
    public PageResult<EventManagementListItemResult> findAllEvents(EventSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(eventJpaEntity.id.count())
            .from(eventJpaEntity)
            .where(
                eventJpaEntity.deleted.isFalse(),
                nameContains(condition.name()),
                statusEq(condition.status())
            )
            .fetchOne();

        List<EventManagementListItemResult> events = queryFactory
            .select(new QEventManagementListItemResult(
                eventJpaEntity.id,
                eventJpaEntity.name,
                eventJpaEntity.status,
                eventJpaEntity.thumbnailImageFileId,
                uploadedFile.originalFilename,
                uploadedFile.filePath,
                eventJpaEntity.startAt,
                eventJpaEntity.endAt
            ))
            .from(eventJpaEntity)
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(eventJpaEntity.thumbnailImageFileId))
            .where(
                eventJpaEntity.deleted.isFalse(),
                nameContains(condition.name()),
                statusEq(condition.status())
            )
            .orderBy(eventJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(events, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
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

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? eventJpaEntity.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression statusEq(EventStatus status) {
        return status != null ? eventJpaEntity.status.eq(status) : null;
    }
}
