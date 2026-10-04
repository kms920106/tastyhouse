package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopOrderNoticeUpsertCommand(
    Long ceoId,
    Long shopId,
    String content
) {

    public ShopOrderNoticeUpsertCommand {
        if (ceoId == null || shopId == null || content == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
