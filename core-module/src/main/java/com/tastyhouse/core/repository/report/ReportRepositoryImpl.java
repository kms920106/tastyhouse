package com.tastyhouse.core.repository.report;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.report.BugReport;
import com.tastyhouse.core.entity.report.BugReportImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.tastyhouse.core.entity.report.QBugReport.bugReport;
import static com.tastyhouse.core.entity.report.QBugReportImage.bugReportImage;

@Repository
@RequiredArgsConstructor
public class ReportRepositoryImpl implements ReportRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<BugReport> findBugReportsByMemberIdOrderByCreatedAtDesc(Long memberId) {
        return queryFactory
            .selectFrom(bugReport)
            .where(bugReport.memberId.eq(memberId))
            .orderBy(bugReport.createdAt.desc())
            .fetch();
    }

    @Override
    public List<BugReportImage> findBugReportImagesByBugReportId(Long bugReportId) {
        return queryFactory
            .selectFrom(bugReportImage)
            .where(bugReportImage.bugReportId.eq(bugReportId))
            .orderBy(bugReportImage.sort.asc())
            .fetch();
    }
}
