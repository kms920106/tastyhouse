package com.tastyhouse.core.domain.bug.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.bug.domain.model.BugReport;
import com.tastyhouse.core.domain.bug.domain.model.BugReportCategory;
import com.tastyhouse.core.domain.bug.domain.model.BugReportPriority;
import com.tastyhouse.core.domain.bug.domain.model.BugReportStatus;
import com.tastyhouse.core.domain.bug.domain.repository.BugReportRepository;
import com.tastyhouse.core.domain.bug.domain.vo.BugReportId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.bug.application.dto.BugReportSearchCondition;
import com.tastyhouse.core.domain.bug.application.dto.result.BugReportListItemResult;
import com.tastyhouse.core.domain.bug.application.dto.result.QBugReportListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.bug.domain.model.QBugReport.bugReport;
import static com.tastyhouse.core.domain.bug.domain.model.QBugReportImage.bugReportImage;

@Repository
@RequiredArgsConstructor
public class BugReportRepositoryImpl implements BugReportRepository {

    private final JPAQueryFactory queryFactory;
    private final BugReportJpaRepository bugReportJpaRepository;

    @Override
    public PageResult<BugReportListItemResult> findAllBugReports(BugReportSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(bugReport.id.count())
            .from(bugReport)
            .where(
                titleContains(condition.title()),
                contentContains(condition.content()),
                memberIdEq(condition.memberId()),
                statusEq(condition.status()),
                categoryEq(condition.category()),
                priorityEq(condition.priority())
            )
            .fetchOne();

        List<BugReportListItemResult> bugReports = queryFactory
            .select(new QBugReportListItemResult(
                bugReport.id,
                bugReport.memberId,
                bugReport.device,
                bugReport.title,
                bugReport.status,
                bugReport.category,
                bugReport.priority,
                JPAExpressions
                    .select(bugReportImage.count())
                    .from(bugReportImage)
                    .where(bugReportImage.bugReportId.eq(bugReport.id)),
                bugReport.createdAt
            ))
            .from(bugReport)
            .where(
                titleContains(condition.title()),
                contentContains(condition.content()),
                memberIdEq(condition.memberId()),
                statusEq(condition.status()),
                categoryEq(condition.category()),
                priorityEq(condition.priority())
            )
            .orderBy(bugReport.id.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(bugReports, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<BugReport> findById(BugReportId bugReportId) {
        if (bugReportId == null) {
            return Optional.empty();
        }
        return bugReportJpaRepository.findById(bugReportId.value());
    }

    @Override
    public BugReport save(BugReport bugReport) {
        return bugReportJpaRepository.save(bugReport);
    }

    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? bugReport.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression contentContains(String content) {
        return StringUtils.hasText(content) ? bugReport.content.containsIgnoreCase(content) : null;
    }

    private BooleanExpression memberIdEq(MemberId memberId) {
        return memberId != null ? bugReport.memberId.eq(memberId) : null;
    }

    private BooleanExpression statusEq(BugReportStatus status) {
        return status != null ? bugReport.status.eq(status) : null;
    }

    private BooleanExpression categoryEq(BugReportCategory category) {
        return category != null ? bugReport.category.eq(category) : null;
    }

    private BooleanExpression priorityEq(BugReportPriority priority) {
        return priority != null ? bugReport.priority.eq(priority) : null;
    }
}
