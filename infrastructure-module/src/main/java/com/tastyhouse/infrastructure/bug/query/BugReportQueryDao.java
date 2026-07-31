package com.tastyhouse.infrastructure.bug.query;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.bug.domain.model.BugReportCategory;
import com.tastyhouse.domain.bug.domain.model.BugReportPriority;
import com.tastyhouse.domain.bug.domain.model.BugReportStatus;
import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import static com.tastyhouse.infrastructure.bug.persistence.QBugReportImageJpaEntity.bugReportImageJpaEntity;
import static com.tastyhouse.infrastructure.bug.persistence.QBugReportJpaEntity.bugReportJpaEntity;

/**
 * 버그 제보 read 어댑터(CQRS query 측).
 *
 * <p>표현 목적 조회를 JPA 엔티티에서 Result DTO로 직접 투영한다. 도메인 모델을 거치지 않으므로
 * write 포트({@code BugReportRepository}/{@code BugReportImageRepository})와 역할이 겹치지 않는다.
 * 소비 모듈(admin-api)의 {@code BugReportQueryService}가 이 DAO를 주입해 사용하며, 그 덕분에 api
 * 모듈은 QueryDSL을 알지 않는다.
 *
 * <p>도메인당 DAO 1개 원칙에 따라 소비자별 메서드를 이 한 클래스에 둔다. 버그 제보 조회는 관리자만
 * 소비하므로(web-api는 제보 등록만 한다) 메서드명에 admin 마커를 붙이지 않고 순수 동작명을 쓴다.
 */
@Repository
@RequiredArgsConstructor
public class BugReportQueryDao {

    private final JPAQueryFactory queryFactory;

    /**
     * 관리 목록 조회 — 제목/내용 부분일치·회원·처리상태·분류·우선순위 필터를 적용하고 첨부 이미지 개수를
     * 서브쿼리 count로 함께 투영한다.
     */
    public PageResult<BugReportListItemResult> findBugReports(BugReportSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(bugReportJpaEntity.id.count())
            .from(bugReportJpaEntity)
            .where(
                titleContains(condition.title()),
                contentContains(condition.content()),
                memberIdEq(condition.memberId()),
                statusEq(condition.status()),
                categoryEq(condition.category()),
                priorityEq(condition.priority())
            )
            .fetchOne();

        List<BugReportListItemResult> items = queryFactory
            .select(new QBugReportListItemResult(
                bugReportJpaEntity.id,
                bugReportJpaEntity.memberId,
                bugReportJpaEntity.device,
                bugReportJpaEntity.title,
                bugReportJpaEntity.status,
                bugReportJpaEntity.category,
                bugReportJpaEntity.priority,
                JPAExpressions
                    .select(bugReportImageJpaEntity.count())
                    .from(bugReportImageJpaEntity)
                    .where(bugReportImageJpaEntity.bugReportId.eq(bugReportJpaEntity.id)),
                bugReportJpaEntity.createdAt
            ))
            .from(bugReportJpaEntity)
            .where(
                titleContains(condition.title()),
                contentContains(condition.content()),
                memberIdEq(condition.memberId()),
                statusEq(condition.status()),
                categoryEq(condition.category()),
                priorityEq(condition.priority())
            )
            .orderBy(bugReportJpaEntity.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(items, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    /**
     * 관리 상세 조회 — 스칼라 필드를 투영한 뒤 첨부 이미지 파일 ID 목록을 정렬 순서대로 합쳐 조립한다.
     */
    public Optional<BugReportDetailResult> findDetailById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        BugReportDetailProjection projection = queryFactory
            .select(new QBugReportDetailProjection(
                bugReportJpaEntity.id,
                bugReportJpaEntity.memberId,
                bugReportJpaEntity.device,
                bugReportJpaEntity.title,
                bugReportJpaEntity.content,
                bugReportJpaEntity.status,
                bugReportJpaEntity.category,
                bugReportJpaEntity.priority,
                bugReportJpaEntity.assigneeAdminId,
                bugReportJpaEntity.adminAnswer,
                bugReportJpaEntity.resolvedAt,
                bugReportJpaEntity.appVersion,
                bugReportJpaEntity.platform,
                bugReportJpaEntity.osVersion,
                bugReportJpaEntity.createdAt,
                bugReportJpaEntity.updatedAt
            ))
            .from(bugReportJpaEntity)
            .where(bugReportJpaEntity.id.eq(id))
            .fetchOne();

        if (projection == null) {
            return Optional.empty();
        }

        List<Long> imageFileIds = findImageFileIds(id);
        return Optional.of(BugReportDetailResult.from(projection, imageFileIds));
    }

    private List<Long> findImageFileIds(Long bugReportId) {
        return queryFactory
            .select(bugReportImageJpaEntity.imageFileId)
            .from(bugReportImageJpaEntity)
            .where(bugReportImageJpaEntity.bugReportId.eq(bugReportId))
            .orderBy(bugReportImageJpaEntity.sort.asc())
            .fetch();
    }

    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? bugReportJpaEntity.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression contentContains(String content) {
        return StringUtils.hasText(content) ? bugReportJpaEntity.content.containsIgnoreCase(content) : null;
    }

    private BooleanExpression memberIdEq(MemberId memberId) {
        return memberId != null ? bugReportJpaEntity.memberId.eq(memberId) : null;
    }

    private BooleanExpression statusEq(BugReportStatus status) {
        return status != null ? bugReportJpaEntity.status.eq(status) : null;
    }

    private BooleanExpression categoryEq(BugReportCategory category) {
        return category != null ? bugReportJpaEntity.category.eq(category) : null;
    }

    private BooleanExpression priorityEq(BugReportPriority priority) {
        return priority != null ? bugReportJpaEntity.priority.eq(priority) : null;
    }
}
