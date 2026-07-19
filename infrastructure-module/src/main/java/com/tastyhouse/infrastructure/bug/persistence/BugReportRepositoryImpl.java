package com.tastyhouse.infrastructure.bug.persistence;

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

import static com.tastyhouse.infrastructure.bug.persistence.QBugReportImageJpaEntity.bugReportImageJpaEntity;
import static com.tastyhouse.infrastructure.bug.persistence.QBugReportJpaEntity.bugReportJpaEntity;

@Repository
@RequiredArgsConstructor
public class BugReportRepositoryImpl implements BugReportRepository {

    private final JPAQueryFactory queryFactory;
    private final BugReportJpaRepository bugReportJpaRepository;

    @Override
    public PageResult<BugReportListItemResult> findAllBugReports(BugReportSearchCondition condition, PageQuery pageQuery) {
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

        List<BugReportListItemResult> bugReports = queryFactory
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

        return PageResult.of(bugReports, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<BugReport> findById(BugReportId bugReportId) {
        if (bugReportId == null) {
            return Optional.empty();
        }
        return bugReportJpaRepository.findById(bugReportId.value())
            .map(BugReportMapper::toDomain);
    }

    @Override
    public BugReport save(BugReport bugReport) {
        if (bugReport.getId() == null) {
            BugReportJpaEntity saved = bugReportJpaRepository.save(BugReportMapper.toEntity(bugReport));
            return BugReportMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        BugReportJpaEntity entity = bugReportJpaRepository.findById(bugReport.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 버그 신고입니다: " + bugReport.getId()));
        BugReportMapper.applyChanges(entity, bugReport);
        return BugReportMapper.toDomain(entity);
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
