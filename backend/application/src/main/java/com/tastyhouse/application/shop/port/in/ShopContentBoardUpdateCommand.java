package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
