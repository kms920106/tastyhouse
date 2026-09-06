package com.tastyhouse.infrastructure.event.query;

import com.tastyhouse.application.event.port.out.EventManagementQueryPort;
import com.tastyhouse.application.event.port.out.EventQueryPort;
import com.tastyhouse.application.event.port.out.EventAnnouncementResult;
import com.tastyhouse.application.event.port.out.EventDetailResult;
import com.tastyhouse.application.event.port.out.EventListItemResult;
import com.tastyhouse.application.event.port.out.EventManagementDetailResult;
import com.tastyhouse.application.event.port.out.EventManagementListItemResult;
import com.tastyhouse.application.event.port.out.EventSearchCondition;
import com.tastyhouse.application.event.port.out.EventWinnerResult;
import com.querydsl.core.types.Projections;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.event.model.EventStatus;
import com.tastyhouse.domain.event.vo.EventId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.event.persistence.QEventAnnouncementJpaEntity.eventAnnouncementJpaEntity;
import static com.tastyhouse.infrastructure.event.persistence.QEventJpaEntity.eventJpaEntity;
import static com.tastyhouse.infrastructure.event.persistence.QEventWinnerJpaEntity.eventWinnerJpaEntity;
import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;

@Repository
public class EventQueryDao implements EventQueryPort, EventManagementQueryPort {
    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public EventQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public PageResult<EventListItemResult> findEventListItemsByStatus(EventStatus status, PageQuery pageQuery) {
        List<EventListItemResult> content = queryFactory
            .select(Projections.constructor(EventListItemResult.class,
                eventJpaEntity.id,
                eventJpaEntity.name,
                uploadedFileJpaEntity.filePath,
                eventJpaEntity.startAt,
                eventJpaEntity.endAt
            ))
            .from(eventJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(eventJpaEntity.thumbnailImageFileId.eq(uploadedFileJpaEntity.id))
            .where(eventJpaEntity.status.eq(status))
            .orderBy(eventJpaEntity.startAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedThumbnailUrl)
            .toList();

        Long total = queryFactory
            .select(eventJpaEntity.count())
            .from(eventJpaEntity)
            .where(eventJpaEntity.status.eq(status))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<EventDetailResult> findEventBannerById(EventId eventId) {
        EventDetailResult result = queryFactory
            .select(Projections.constructor(EventDetailResult.class,
                uploadedFileJpaEntity.filePath
            ))
            .from(eventJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(eventJpaEntity.bannerImageFileId.eq(uploadedFileJpaEntity.id))
            .where(eventJpaEntity.id.eq(eventId.value()))
            .fetchOne();

        return Optional.ofNullable(result).map(this::withResolvedBannerUrl);
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

        List<EventManagementListItemResult> content = queryFactory
            .select(Projections.constructor(EventManagementListItemResult.class,
                eventJpaEntity.id,
                eventJpaEntity.name,
                eventJpaEntity.status,
                eventJpaEntity.thumbnailImageFileId,
                uploadedFileJpaEntity.originalFilename,
                uploadedFileJpaEntity.filePath,
                eventJpaEntity.startAt,
                eventJpaEntity.endAt
            ))
            .from(eventJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(eventJpaEntity.thumbnailImageFileId))
            .where(
                eventJpaEntity.deleted.isFalse(),
                nameContains(condition.name()),
                statusEq(condition.status())
            )
            .orderBy(eventJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedThumbnailUrl)
            .toList();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<EventManagementDetailResult> findEventDetailById(EventId eventId) {
        QUploadedFileJpaEntity thumbnailFile = new QUploadedFileJpaEntity("thumbnailFile");
        QUploadedFileJpaEntity bannerFile = new QUploadedFileJpaEntity("bannerFile");

        EventManagementDetailResult detail = queryFactory
            .select(Projections.constructor(EventManagementDetailResult.class,
                eventJpaEntity.id,
                eventJpaEntity.name,
                eventJpaEntity.description,
                eventJpaEntity.subtitle,
                eventJpaEntity.thumbnailImageFileId,
                thumbnailFile.originalFilename,
                thumbnailFile.filePath,
                eventJpaEntity.bannerImageFileId,
                bannerFile.originalFilename,
                bannerFile.filePath,
                eventJpaEntity.contentHtml,
                eventJpaEntity.status,
                eventJpaEntity.startAt,
                eventJpaEntity.endAt,
                eventJpaEntity.createdAt,
                eventJpaEntity.updatedAt
            ))
            .from(eventJpaEntity)
            .leftJoin(thumbnailFile).on(thumbnailFile.id.eq(eventJpaEntity.thumbnailImageFileId))
            .leftJoin(bannerFile).on(bannerFile.id.eq(eventJpaEntity.bannerImageFileId))
            .where(eventJpaEntity.id.eq(eventId.value()), eventJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(detail).map(this::withResolvedFileUrls);
    }

    @Override
    public List<EventWinnerResult> findWinnersByEventId(EventId eventId) {
        return queryFactory
            .select(Projections.constructor(EventWinnerResult.class,
                eventWinnerJpaEntity.id,
                eventWinnerJpaEntity.eventId,
                eventWinnerJpaEntity.rankNo,
                eventWinnerJpaEntity.winnerName,
                eventWinnerJpaEntity.phoneNumber.value,
                eventWinnerJpaEntity.announcedAt
            ))
            .from(eventWinnerJpaEntity)
            .where(eventWinnerJpaEntity.eventId.eq(eventId.value()), eventWinnerJpaEntity.deleted.isFalse())
            .orderBy(eventWinnerJpaEntity.rankNo.asc())
            .fetch();
    }

    @Override
    public Optional<EventAnnouncementResult> findAnnouncementByEventId(EventId eventId) {
        EventAnnouncementResult result = selectAnnouncement()
            .where(eventAnnouncementJpaEntity.eventId.eq(eventId.value()))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public PageResult<EventAnnouncementResult> findAnnouncements(PageQuery pageQuery) {
        List<EventAnnouncementResult> content = selectAnnouncement()
            .orderBy(eventAnnouncementJpaEntity.announcedAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Long total = queryFactory
            .select(eventAnnouncementJpaEntity.count())
            .from(eventAnnouncementJpaEntity)
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    private JPAQuery<EventAnnouncementResult> selectAnnouncement() {
        return queryFactory
            .select(Projections.constructor(EventAnnouncementResult.class,
                eventAnnouncementJpaEntity.id,
                eventAnnouncementJpaEntity.eventId,
                eventAnnouncementJpaEntity.name,
                eventAnnouncementJpaEntity.content,
                eventAnnouncementJpaEntity.announcedAt
            ))
            .from(eventAnnouncementJpaEntity);
    }

    private EventListItemResult withResolvedThumbnailUrl(EventListItemResult row) {
        return new EventListItemResult(
            row.eventId(),
            row.name(),
            fileUrlResolver.resolve(row.thumbnailUrl()),
            row.startAt(),
            row.endAt()
        );
    }

    private EventManagementListItemResult withResolvedThumbnailUrl(EventManagementListItemResult row) {
        return new EventManagementListItemResult(
            row.id(),
            row.name(),
            row.status(),
            row.thumbnailImageFileId(),
            row.thumbnailFileName(),
            fileUrlResolver.resolve(row.thumbnailUrl()),
            row.startAt(),
            row.endAt()
        );
    }

    private EventDetailResult withResolvedBannerUrl(EventDetailResult row) {
        return new EventDetailResult(
            fileUrlResolver.resolve(row.bannerUrl())
        );
    }

    private EventManagementDetailResult withResolvedFileUrls(EventManagementDetailResult row) {
        return new EventManagementDetailResult(
            row.id(),
            row.name(),
            row.description(),
            row.subtitle(),
            row.thumbnailImageFileId(),
            row.thumbnailFileName(),
            fileUrlResolver.resolve(row.thumbnailUrl()),
            row.bannerImageFileId(),
            row.bannerFileName(),
            fileUrlResolver.resolve(row.bannerUrl()),
            row.contentHtml(),
            row.status(),
            row.startAt(),
            row.endAt(),
            row.createdAt(),
            row.updatedAt()
        );
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? eventJpaEntity.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression statusEq(EventStatus status) {
        return status != null ? eventJpaEntity.status.eq(status) : null;
    }
}
