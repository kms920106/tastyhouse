package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
