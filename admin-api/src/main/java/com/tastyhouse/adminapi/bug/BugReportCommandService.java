package com.tastyhouse.adminapi.bug;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.admin.domain.vo.AdminId;
import com.tastyhouse.domain.bug.domain.model.BugReport;
import com.tastyhouse.domain.bug.domain.model.BugReportCategory;
import com.tastyhouse.domain.bug.domain.model.BugReportPriority;
import com.tastyhouse.domain.bug.domain.model.BugReportStatus;
import com.tastyhouse.domain.bug.domain.repository.BugReportRepository;
import com.tastyhouse.domain.bug.domain.vo.BugReportId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 버그 제보 관리 command 서비스.
 *
 * <p>상태 전이·분류·담당자 배정은 모두 제보 애그리거트 하나만 다루는 액터(admin) 특화 command이므로
 * 도메인 서비스로 하강시키지 않고 이 서비스가 직접 수행한다(공통 지침 패턴 2). domain write
 * 포트({@link BugReportRepository})만 주입하며, 조회는 {@link BugReportQueryService}가 담당한다.
 *
 * <p>{@code BugReport}는 순수 POJO라 더티 체킹이 없으므로 도메인 변경 후 명시적으로
 * {@code bugReportRepository.save(bugReport)}를 호출한다.
 */
@Service
@Transactional
public class BugReportCommandService {

    private final BugReportRepository bugReportRepository;

    public BugReportCommandService(BugReportRepository bugReportRepository) {
        this.bugReportRepository = bugReportRepository;
    }

    public void changeStatus(Long id, String status, String answer) {
        BugReportId bugReportId = BugReportId.of(id);
        BugReportStatus bugReportStatus = BugReportStatus.from(status);
        BugReport bugReport = findBugReportOrThrow(bugReportId);

        switch (bugReportStatus) {
            case IN_PROGRESS -> bugReport.startProgress();
            case RESOLVED -> bugReport.resolve(answer);
            case REJECTED -> bugReport.reject(answer);
            case ON_HOLD -> bugReport.hold();
            case RECEIVED -> throw new BusinessException(ErrorCode.BUG_REPORT_INVALID_STATUS);
        }

        bugReportRepository.save(bugReport);
    }

    public void classify(Long id, String category, String priority) {
        BugReportId bugReportId = BugReportId.of(id);
        BugReportCategory bugReportCategory = BugReportCategory.from(category);
        BugReportPriority bugReportPriority = BugReportPriority.from(priority);
        BugReport bugReport = findBugReportOrThrow(bugReportId);

        bugReport.classify(bugReportCategory, bugReportPriority);
        bugReportRepository.save(bugReport);
    }

    public void assign(Long id, Long assigneeAdminId) {
        BugReportId bugReportId = BugReportId.of(id);
        BugReport bugReport = findBugReportOrThrow(bugReportId);

        AdminId adminId = AdminId.of(assigneeAdminId);
        bugReport.assignTo(adminId);
        bugReportRepository.save(bugReport);
    }

    private BugReport findBugReportOrThrow(BugReportId bugReportId) {
        return bugReportRepository.findById(bugReportId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.BUG_REPORT_NOT_FOUND));
    }
}
