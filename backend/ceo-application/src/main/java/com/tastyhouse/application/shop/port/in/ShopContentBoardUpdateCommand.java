package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopContentBoardUpdateCommand(
    Long ceoId,
    Long shopId,
    Long contentBoardId,
    String topic,
    String youtubeUrl,
    String description
) {

    public ShopContentBoardUpdateCommand {
        if (ceoId == null || shopId == null || contentBoardId == null || topic == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
