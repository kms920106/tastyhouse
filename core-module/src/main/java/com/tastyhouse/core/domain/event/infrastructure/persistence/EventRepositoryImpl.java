package com.tastyhouse.core.domain.event.infrastructure.persistence;

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
import com.tastyhouse.core.domain.event.application.dto.EventDetailDto;
import com.tastyhouse.core.domain.event.application.dto.EventListItemDto;
import com.tastyhouse.core.domain.event.application.dto.EventManagementListItemDto;
import com.tastyhouse.core.domain.event.application.dto.EventSearchCondition;
import com.tastyhouse.core.domain.event.application.dto.QEventDetailDto;
import com.tastyhouse.core.domain.event.application.dto.QEventListItemDto;
import com.tastyhouse.core.domain.event.application.dto.QEventManagementListItemDto;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.event.domain.model.QEvent.event;
import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;

@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {

    private final JPAQueryFactory queryFactory;
    private final EventJpaRepository eventJpaRepository;

    @Override
    public PageResult<EventListItemDto> findEventListItemsByStatus(EventStatus status, PageQuery pageQuery) {
        List<EventListItemDto> content = queryFactory
            .select(new QEventListItemDto(
                event.id,
                event.name,
                uploadedFile.filePath,
                event.startAt,
                event.endAt
            ))
            .from(event)
            .leftJoin(uploadedFile).on(event.thumbnailImageFileId.eq(uploadedFile.id))
            .where(event.status.eq(status))
            .orderBy(event.startAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(event.count())
            .from(event)
            .where(event.status.eq(status));

        Long total = countQuery.fetchOne();
        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<EventDetailDto> findEventDetailById(EventId eventId) {
        EventDetailDto result = queryFactory
            .select(new QEventDetailDto(
                uploadedFile.filePath
            ))
            .from(event)
            .leftJoin(uploadedFile).on(event.bannerImageFileId.eq(uploadedFile.id))
            .where(event.id.eq(eventId.value()))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Event> findById(EventId eventId) {
        return Optional.ofNullable(queryFactory
            .selectFrom(event)
            .where(event.id.eq(eventId.value()), event.deleted.isFalse())
            .fetchOne());
    }

    @Override
    public PageResult<EventManagementListItemDto> findAllEvents(EventSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(event.id.count())
            .from(event)
            .where(
                event.deleted.isFalse(),
                nameContains(condition.name()),
                statusEq(condition.status())
            )
            .fetchOne();

        List<EventManagementListItemDto> events = queryFactory
            .select(new QEventManagementListItemDto(
                event.id,
                event.name,
                event.status,
                event.thumbnailImageFileId,
                uploadedFile.originalFilename,
                uploadedFile.filePath,
                event.startAt,
                event.endAt
            ))
            .from(event)
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(event.thumbnailImageFileId))
            .where(
                event.deleted.isFalse(),
                nameContains(condition.name()),
                statusEq(condition.status())
            )
            .orderBy(event.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(events, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Event save(Event newEvent) {
        return eventJpaRepository.save(newEvent);
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? event.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression statusEq(EventStatus status) {
        return status != null ? event.status.eq(status) : null;
    }
}
