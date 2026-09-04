package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 라이더 방문안내 문구 삭제 command. */
public record ShopRiderVisitGuideDeleteCommand(
    Long shopId,
    Long adminId,
    String reason
) {
    public ShopRiderVisitGuideDeleteCommand {
        if (shopId == null || adminId == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
