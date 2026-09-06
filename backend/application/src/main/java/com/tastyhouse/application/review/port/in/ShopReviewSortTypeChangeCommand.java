package com.tastyhouse.application.review.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ShopReviewSortTypeChangeCommand(
    Long ceoId,
    Long shopId,
    String sortType
) {
    public ShopReviewSortTypeChangeCommand {
        if (ceoId == null || shopId == null || sortType == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
