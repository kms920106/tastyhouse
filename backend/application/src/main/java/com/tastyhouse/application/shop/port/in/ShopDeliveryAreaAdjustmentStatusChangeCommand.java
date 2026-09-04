package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 배달권역 조정 신청 상태 전이 command. {@code status}는 경계에서 문자열로 받고 서비스가 enum으로 승격한다. */
public record ShopDeliveryAreaAdjustmentStatusChangeCommand(
    Long requestId,
    String status
) {
    public ShopDeliveryAreaAdjustmentStatusChangeCommand {
        if (requestId == null || status == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
