package com.tastyhouse.infrastructure.event.query;

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

/**
 * 이벤트 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로 write
 * 포트({@code EventRepository}/{@code EventWinnerRepository}/{@code EventAnnouncementRepository})와 역할이
 * 겹치지 않는다. 소비 모듈(web-api/admin-api)의 {@code EventQueryService}가 이 DAO를 주입해 사용하며,
 * 그 덕분에 api 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>도메인당 DAO 1개 원칙에 따라 세 애그리거트(이벤트·당첨자·발표)의 소비자별 메서드를 이 한 클래스에
 * 둔다. 메서드명에는 admin 마커를 붙이지 않고 순수 동작명을 쓰며, admin의 관리 목록·상세
 * ({@code findAllEvents}/{@code findEventDetailById})와 web의 노출 목록·상세
 * ({@code findEventListItemsByStatus}/{@code findEventBannerById})는 시그니처·의미 있는 한정어로 구분한다.
 *
 * <p>썸네일·배너 파일 경로는 같은 모듈의 {@code UploadedFileJpaEntity}를 left join해 얻는다(파일 미등록
 * 이벤트도 목록에서 누락되지 않도록 inner join을 쓰지 않는다). 조인으로 얻은 저장 경로는
 * {@link FileUrlResolver}로 표시용 URL까지 변환해 Result에 담는다 — {@code Projections.constructor}는 record
 * 생성자로 직접 투영하므로 변환을 투영식에 끼울 수 없어, fetch 직후 재조립한다.
 *
 * <p>삭제 필터링은 이관 이전 동작을 그대로 보존한다 — admin 관리 목록/상세와 당첨자 목록은 soft delete
 * 분을 제외하고, web 노출 목록/상세와 발표 목록은 원본 쿼리에 삭제 필터가 없었으므로 추가하지 않는다.
 */
@Repository
public class EventQueryDao implements EventQueryPort {

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public EventQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 상태별 이벤트 목록 페이징 조회(web 노출 목록) — 시작 일시 내림차순.
     */
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

    /**
     * 이벤트 배너 이미지 URL 조회(web 상세) — 이벤트가 없으면 비어 있다(소비 측에서 404로 변환).
     */
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

    /**
     * 이벤트 관리 목록 페이징 조회(admin) — 이벤트명 부분일치·상태 필터를 선택적으로 적용한다.
     */
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

    /**
     * 이벤트 관리 상세 조회(admin) — 삭제된 이벤트면 비어 있다(소비 측에서 404로 변환). 썸네일·배너
     * 이미지를 각각 별도 alias로 left join해 파일명·URL까지 함께 투영한다(추가 조회 없음).
     */
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

    /**
     * 이벤트의 당첨자 목록 조회(admin) — 순위 오름차순, 삭제분 제외.
     */
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

    /**
     * 이벤트의 당첨자 발표 단건 조회(admin) — 발표가 없으면 비어 있다(소비 측에서 404로 변환).
     */
    @Override
    public Optional<EventAnnouncementResult> findAnnouncementByEventId(EventId eventId) {
        EventAnnouncementResult result = selectAnnouncement()
            .where(eventAnnouncementJpaEntity.eventId.eq(eventId.value()))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 전체 이벤트의 당첨자 발표 목록 페이징 조회(web) — 발표 일시 내림차순.
     */
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

    /**
     * 발표 조회 두 메서드가 공유하는 투영 — where·정렬만 각자 덧붙인다.
     */
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

    /**
     * 투영된 저장 경로를 표시용 URL로 바꿔 재조립한다. 아래 세 메서드는 {@code Projections.constructor}가
     * 생성자 직접 투영이라 변환을 투영식에 넣을 수 없어 fetch 직후 호출한다.
     */
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
