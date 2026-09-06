package com.tastyhouse.infrastructure.bug.query;

import com.tastyhouse.application.bug.port.out.BugReportQueryPort;
import com.tastyhouse.application.bug.port.out.BugReportDetailResult;
import com.tastyhouse.application.bug.port.out.BugReportImageResult;
import com.tastyhouse.application.bug.port.out.BugReportListItemResult;
import com.tastyhouse.application.bug.port.out.BugReportSearchCondition;
import com.querydsl.core.types.Projections;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.bug.persistence.QBugReportImageJpaEntity.bugReportImageJpaEntity;
import static com.tastyhouse.infrastructure.bug.persistence.QBugReportJpaEntity.bugReportJpaEntity;
import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;

@Repository
public class BugReportQueryDao implements BugReportQueryPort {
    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public BugReportQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
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
            .select(Projections.constructor(BugReportListItemResult.class,
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

    @Override
    public Optional<BugReportDetailResult> findDetailById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        BugReportDetailProjection projection = queryFactory
            .select(Projections.constructor(BugReportDetailProjection.class,
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

        List<BugReportImageResult> images = findImages(id);
        return Optional.of(toDetailResult(projection, images));
    }

    private List<BugReportImageResult> findImages(Long bugReportId) {
        return queryFactory
            .select(Projections.constructor(BugReportImageResult.class,
                bugReportImageJpaEntity.imageFileId,
                uploadedFileJpaEntity.originalFilename,
                uploadedFileJpaEntity.filePath
            ))
            .from(bugReportImageJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(bugReportImageJpaEntity.imageFileId))
            .where(bugReportImageJpaEntity.bugReportId.eq(bugReportId))
            .orderBy(bugReportImageJpaEntity.sort.asc())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();
    }

    private BugReportImageResult withResolvedImageUrl(BugReportImageResult row) {
        return new BugReportImageResult(
            row.fileId(),
            row.fileName(),
            fileUrlResolver.resolve(row.imageUrl())
        );
    }

    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? bugReportJpaEntity.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression contentContains(String content) {
        return StringUtils.hasText(content) ? bugReportJpaEntity.content.containsIgnoreCase(content) : null;
    }

    private BooleanExpression memberIdEq(Long memberId) {
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

    private BugReportDetailResult toDetailResult(BugReportDetailProjection projection,
                                                 List<BugReportImageResult> images) {
        return new BugReportDetailResult(
            projection.id(),
            projection.memberId(),
            projection.device(),
            projection.title(),
            projection.content(),
            projection.status(),
            projection.category(),
            projection.priority(),
            projection.assigneeAdminId(),
            projection.adminAnswer(),
            projection.resolvedAt(),
            projection.appVersion(),
            projection.platform(),
            projection.osVersion(),
            images,
            projection.createdAt(),
            projection.updatedAt()
        );
    }
}
