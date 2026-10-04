package com.tastyhouse.application.review.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewCreateCommand(
    Long memberId,
    Long orderProductId,
    Long productId,
    Integer tasteRating,
    Integer amountRating,
    Integer priceRating,
    String content,
    List<Long> uploadedFileIds,
    List<String> tags,
    Boolean ownerOnly,
    Integer deliveryRating,
    String deliveryComment
) {

    public ReviewCreateCommand {
        if (memberId == null || productId == null || tasteRating == null
            || amountRating == null || priceRating == null || content == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
