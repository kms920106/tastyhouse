package com.tastyhouse.adminapi.shop.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/** 주문안내 재게시 command. */
public record ShopOrderNoticeUnhideCommand(
    Long shopId
) {
    public ShopOrderNoticeUnhideCommand {
        if (shopId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static ShopOrderNoticeUnhideCommand of(Long shopId) {
        return new ShopOrderNoticeUnhideCommand(shopId);
    }}
