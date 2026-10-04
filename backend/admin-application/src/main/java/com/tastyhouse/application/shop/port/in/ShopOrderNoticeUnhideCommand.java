package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopOrderNoticeUnhideCommand(
    Long shopId
) {

    public ShopOrderNoticeUnhideCommand {
        if (shopId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }

    public static ShopOrderNoticeUnhideCommand of(Long shopId) {
        return new ShopOrderNoticeUnhideCommand(shopId);
    }}
