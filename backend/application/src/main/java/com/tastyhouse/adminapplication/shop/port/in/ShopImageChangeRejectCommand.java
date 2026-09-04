package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 이미지 변경 요청 반려 command. */
public record ShopImageChangeRejectCommand(
    Long requestId,
    String reason
) {
    public ShopImageChangeRejectCommand {
        if (requestId == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
