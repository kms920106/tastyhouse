package com.tastyhouse.adminapplication.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 가게 이미지 변경 요청 승인 command. */
public record ShopImageChangeApproveCommand(
    Long requestId
) {
    public ShopImageChangeApproveCommand {
        if (requestId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopImageChangeApproveCommand of(Long requestId) {
        return new ShopImageChangeApproveCommand(requestId);
    }}
