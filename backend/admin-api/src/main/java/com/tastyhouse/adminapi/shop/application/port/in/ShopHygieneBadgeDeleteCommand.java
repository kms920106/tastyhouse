package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 위생 인증 뱃지 삭제 command. */
public record ShopHygieneBadgeDeleteCommand(
    Long hygieneBadgeId
) {
    public ShopHygieneBadgeDeleteCommand {
        if (hygieneBadgeId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopHygieneBadgeDeleteCommand of(Long hygieneBadgeId) {
        return new ShopHygieneBadgeDeleteCommand(hygieneBadgeId);
    }}
