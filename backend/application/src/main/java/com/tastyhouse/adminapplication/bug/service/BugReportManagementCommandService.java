package com.tastyhouse.adminapplication.bug.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.adminapplication.bug.port.in.BugReportAssignCommand;
import com.tastyhouse.adminapplication.bug.port.in.BugReportClassifyCommand;
import com.tastyhouse.adminapplication.bug.port.in.BugReportManagementCommandUseCase;
import com.tastyhouse.adminapplication.bug.port.in.BugReportStatusChangeCommand;
import com.tastyhouse.domain.admin.vo.AdminId;
import com.tastyhouse.domain.bug.model.BugReport;
import com.tastyhouse.domain.bug.model.BugReportCategory;
import com.tastyhouse.domain.bug.model.BugReportPriority;
import com.tastyhouse.domain.bug.model.BugReportStatus;
import com.tastyhouse.domain.bug.repository.BugReportRepository;
import com.tastyhouse.domain.bug.vo.BugReportId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

/**
 * 버그 제보 관리 command 서비스.
 *
 * <p>상태 전이·분류·담당자 배정은 모두 제보 애그리거트 하나만 다루는 액터(admin) 특화 command이므로
 * 도메인 서비스로 하강시키지 않고 이 서비스가 직접 수행한다(공통 지침 패턴 2). domain write
 * 포트({@link BugReportRepository})만 주입하며, 조회는 {@code BugReportQueryService}가 담당한다.
 *
 * <p>{@code BugReport}는 순수 POJO라 더티 체킹이 없으므로 도메인 변경 후 명시적으로
 * {@code bugReportRepository.save(bugReport)}를 호출한다.
 */
@Service
@Transactional
public class BugReportManagementCommandService implements BugReportManagementCommandUseCase {

    private final BugReportRepository bugReportRepository;

    public BugReportManagementCommandService(BugReportRepository bugReportRepository) {
        this.bugReportRepository = bugReportRepository;
    }

    @Override
    public void changeStatus(BugReportStatusChangeCommand command) {
        String answer = command.answer();
        BugReportId bugReportId = BugReportId.of(command.bugReportId());
        BugReportStatus bugReportStatus = BugReportStatus.from(command.status());
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

    @Override
    public void classify(BugReportClassifyCommand command) {
        BugReportId bugReportId = BugReportId.of(command.bugReportId());
        BugReportCategory bugReportCategory = BugReportCategory.from(command.category());
        BugReportPriority bugReportPriority = BugReportPriority.from(command.priority());
        BugReport bugReport = findBugReportOrThrow(bugReportId);

        bugReport.classify(bugReportCategory, bugReportPriority);
        bugReportRepository.save(bugReport);
    }

    @Override
    public void assign(BugReportAssignCommand command) {
        BugReportId bugReportId = BugReportId.of(command.bugReportId());
        BugReport bugReport = findBugReportOrThrow(bugReportId);

        AdminId adminId = AdminId.of(command.assigneeAdminId());
        bugReport.assignTo(adminId);
        bugReportRepository.save(bugReport);
    }

    private BugReport findBugReportOrThrow(BugReportId bugReportId) {
        return bugReportRepository.findById(bugReportId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BUG_REPORT_NOT_FOUND));
    }
}
