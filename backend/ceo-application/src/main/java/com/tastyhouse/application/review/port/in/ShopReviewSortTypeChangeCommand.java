package com.tastyhouse.application.review.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ShopReviewSortTypeChangeCommand(
    Long ceoId,
    Long shopId,
    String sortType
) {

    public ShopReviewSortTypeChangeCommand {
        if (ceoId == null || shopId == null || sortType == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
