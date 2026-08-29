package com.tastyhouse.adminapi.bug.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 버그 제보 담당자 배정 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 *
 * <p>{@code bugReportId}(대상 제보)와 {@code assigneeAdminId}(담당 관리자)는 둘 다 {@code Long}이라
 * 순서가 뒤바뀌어도 컴파일된다. 조립은 반드시 이름 있는 접근자로 한다.
 */
public record BugReportAssignCommand(
    Long bugReportId,
    Long assigneeAdminId
) {
    public BugReportAssignCommand {
        if (bugReportId == null || assigneeAdminId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
