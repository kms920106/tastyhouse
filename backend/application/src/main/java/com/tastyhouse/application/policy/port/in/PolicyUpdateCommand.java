package com.tastyhouse.application.policy.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 약관 수정 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 */
public record PolicyUpdateCommand(
    Long policyDocumentId,
    String title,
    String content,
    boolean mandatory,
    LocalDateTime effectiveDate,
    String updatedBy
) {
    public PolicyUpdateCommand {
        if (policyDocumentId == null || title == null || content == null || effectiveDate == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
