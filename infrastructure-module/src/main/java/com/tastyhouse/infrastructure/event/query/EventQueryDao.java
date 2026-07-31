package com.tastyhouse.infrastructure.event.query;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.event.domain.model.EventStatus;
import com.tastyhouse.domain.event.domain.vo.EventId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

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
 * 이벤트도 목록에서 누락되지 않도록 inner join을 쓰지 않는다).
 *
 * <p>삭제 필터링은 이관 이전 동작을 그대로 보존한다 — admin 관리 목록/상세와 당첨자 목록은 soft delete
 * 분을 제외하고, web 노출 목록/상세와 발표 목록은 원본 쿼리에 삭제 필터가 없었으므로 추가하지 않는다.
 */
@Repository
@RequiredArgsConstructor
public class EventQueryDao {

    private final JPAQueryFactory queryFactory;

    /**
     * 상태별 이벤트 목록 페이징 조회(web 노출 목록) — 시작 일시 내림차순.
     */
    public PageResult<EventListItemResult> findEventListItemsByStatus(EventStatus status, PageQuery pageQuery) {
        List<EventListItemResult> content = queryFactory
            .select(new QEventListItemResult(
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
            .fetch();

        Long total = queryFactory
            .select(eventJpaEntity.count())
            .from(eventJpaEntity)
            .where(eventJpaEntity.status.eq(status))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    /**
     * 이벤트 배너 이미지 경로 조회(web 상세) — 이벤트가 없으면 비어 있다(소비 측에서 404로 변환).
     */
    public Optional<EventDetailResult> findEventBannerById(EventId eventId) {
        EventDetailResult result = queryFactory
            .select(new QEventDetailResult(
                uploadedFileJpaEntity.filePath
            ))
            .from(eventJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(eventJpaEntity.bannerImageFileId.eq(uploadedFileJpaEntity.id))
            .where(eventJpaEntity.id.eq(eventId.value()))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 이벤트 관리 목록 페이징 조회(admin) — 이벤트명 부분일치·상태 필터를 선택적으로 적용한다.
     */
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
            .select(new QEventManagementListItemResult(
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
            .fetch();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    /**
     * 이벤트 관리 상세 조회(admin) — 삭제된 이벤트면 비어 있다(소비 측에서 404로 변환).
     */
    public Optional<EventManagementDetailResult> findEventDetailById(EventId eventId) {
        EventManagementDetailResult detail = queryFactory
            .select(new QEventManagementDetailResult(
                eventJpaEntity.id,
                eventJpaEntity.name,
                eventJpaEntity.description,
                eventJpaEntity.subtitle,
                eventJpaEntity.thumbnailImageFileId,
                eventJpaEntity.bannerImageFileId,
                eventJpaEntity.contentHtml,
                eventJpaEntity.status,
                eventJpaEntity.startAt,
                eventJpaEntity.endAt,
                eventJpaEntity.createdAt,
                eventJpaEntity.updatedAt
            ))
            .from(eventJpaEntity)
            .where(eventJpaEntity.id.eq(eventId.value()), eventJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(detail);
    }

    /**
     * 이벤트의 당첨자 목록 조회(admin) — 순위 오름차순, 삭제분 제외.
     */
    public List<EventWinnerResult> findWinnersByEventId(EventId eventId) {
        return queryFactory
            .select(new QEventWinnerResult(
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
    public Optional<EventAnnouncementResult> findAnnouncementByEventId(EventId eventId) {
        EventAnnouncementResult result = selectAnnouncement()
            .where(eventAnnouncementJpaEntity.eventId.eq(eventId.value()))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    /**
     * 전체 이벤트의 당첨자 발표 목록 페이징 조회(web) — 발표 일시 내림차순.
     */
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
            .select(new QEventAnnouncementResult(
                eventAnnouncementJpaEntity.id,
                eventAnnouncementJpaEntity.eventId,
                eventAnnouncementJpaEntity.name,
                eventAnnouncementJpaEntity.content,
                eventAnnouncementJpaEntity.announcedAt
            ))
            .from(eventAnnouncementJpaEntity);
    }

    private BooleanExpression nameContains(String name) {
        return StringUtils.hasText(name) ? eventJpaEntity.name.containsIgnoreCase(name) : null;
    }

    private BooleanExpression statusEq(EventStatus status) {
        return status != null ? eventJpaEntity.status.eq(status) : null;
    }
}
