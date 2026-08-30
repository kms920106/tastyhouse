package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 라이더 방문안내 수정 요청 command. */
public record ShopRiderVisitGuideRevisionCommand(
    Long shopId,
    Long adminId,
    String reason
) {
    public ShopRiderVisitGuideRevisionCommand {
        if (shopId == null || adminId == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
