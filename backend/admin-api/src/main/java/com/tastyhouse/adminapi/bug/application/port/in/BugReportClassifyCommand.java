package com.tastyhouse.adminapi.bug.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 버그 제보 분류/우선순위 지정 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 */
public record BugReportClassifyCommand(
    Long bugReportId,
    String category,
    String priority
) {
    public BugReportClassifyCommand {
        if (bugReportId == null || category == null || priority == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
