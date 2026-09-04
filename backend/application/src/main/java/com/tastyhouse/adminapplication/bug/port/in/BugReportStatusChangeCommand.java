package com.tastyhouse.adminapplication.bug.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 버그 제보 처리 상태 변경 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 *
 * <p>형식 검증은 Request의 jakarta.validation이 담당하고, 이 record는 구조적 가드만 둔다.
 * {@code answer}는 RESOLVED·REJECTED에서만 기록되는 선택값이라 null을 허용한다.
 */
public record BugReportStatusChangeCommand(
    Long bugReportId,
    String status,
    String answer
) {
    public BugReportStatusChangeCommand {
        if (bugReportId == null || status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
