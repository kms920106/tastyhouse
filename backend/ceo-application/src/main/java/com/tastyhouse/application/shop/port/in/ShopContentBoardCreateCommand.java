package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopContentBoardCreateCommand(
    Long ceoId,
    Long shopId,
    String contentType,
    String topic,
    String youtubeUrl,
    String description
) {

    public ShopContentBoardCreateCommand {
        if (ceoId == null || shopId == null || contentType == null || topic == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
