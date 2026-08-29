package com.tastyhouse.adminapi.partnership.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 제휴 신청 처리 상태 변경 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 */
public record PartnershipStatusChangeCommand(
    Long partnershipRequestId,
    String status
) {
    public PartnershipStatusChangeCommand {
        if (partnershipRequestId == null || status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
