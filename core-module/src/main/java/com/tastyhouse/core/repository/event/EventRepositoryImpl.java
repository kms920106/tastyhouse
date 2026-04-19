package com.tastyhouse.core.repository.event;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.event.Event;
import com.tastyhouse.core.entity.event.EventAnnouncement;
import com.tastyhouse.core.entity.event.EventStatus;
import com.tastyhouse.core.entity.event.EventType;
import com.tastyhouse.core.entity.event.QEvent;
import com.tastyhouse.core.entity.event.QEventAnnouncement;
import com.tastyhouse.core.entity.event.QEventPrize;
import com.tastyhouse.core.entity.event.dto.EventDetailDto;
import com.tastyhouse.core.entity.event.dto.EventListItemDto;
import com.tastyhouse.core.entity.event.dto.PrizeItemDto;
import com.tastyhouse.core.entity.file.QUploadedFile;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Event> findLatestByStatusAndType(EventStatus status, EventType type) {
        QEvent event = QEvent.event;

        Event result = queryFactory
            .selectFrom(event)
            .where(
                event.status.eq(status),
                event.type.eq(type)
            )
            .orderBy(event.startAt.desc())
            .limit(1)
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Page<EventAnnouncement> findAllAnnouncementsOrderByAnnouncedAtDesc(Pageable pageable) {
        QEventAnnouncement eventAnnouncement = QEventAnnouncement.eventAnnouncement;

        List<EventAnnouncement> content = queryFactory
            .selectFrom(eventAnnouncement)
            .orderBy(eventAnnouncement.announcedAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(eventAnnouncement.count())
            .from(eventAnnouncement);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public Page<EventListItemDto> findEventListItemsByStatus(EventStatus status, Pageable pageable) {
        QEvent event = QEvent.event;
        QUploadedFile uploadedFile = QUploadedFile.uploadedFile;

        List<EventListItemDto> content = queryFactory
            .select(Projections.constructor(EventListItemDto.class,
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
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        com.querydsl.jpa.impl.JPAQuery<Long> countQuery = queryFactory
            .select(event.count())
            .from(event)
            .where(event.status.eq(status));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<PrizeItemDto> findPrizeItemsByEventId(Long eventId) {
        QEventPrize eventPrize = QEventPrize.eventPrize;
        QUploadedFile uploadedFile = QUploadedFile.uploadedFile;

        return queryFactory
            .select(Projections.constructor(PrizeItemDto.class,
                eventPrize.id,
                eventPrize.prizeRank,
                eventPrize.name,
                eventPrize.brand,
                uploadedFile.filePath
            ))
            .from(eventPrize)
            .leftJoin(uploadedFile).on(eventPrize.imageFileId.eq(uploadedFile.id))
            .where(eventPrize.eventId.eq(eventId))
            .orderBy(eventPrize.prizeRank.asc())
            .fetch();
    }

    @Override
    public Optional<EventDetailDto> findEventDetailById(Long eventId) {
        QEvent event = QEvent.event;
        QUploadedFile uploadedFile = QUploadedFile.uploadedFile;

        EventDetailDto result = queryFactory
            .select(Projections.constructor(EventDetailDto.class,
                uploadedFile.filePath
            ))
            .from(event)
            .leftJoin(uploadedFile).on(event.bannerImageFileId.eq(uploadedFile.id))
            .where(event.id.eq(eventId))
            .fetchOne();

        return Optional.ofNullable(result);
    }
}
