package com.tastyhouse.application.review.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ReviewUpdateCommand(
    Long memberId,
    Long reviewId,
    Integer tasteRating,
    Integer amountRating,
    Integer priceRating,
    String content,
    List<Long> uploadedFileIds,
    List<String> tags,
    Integer deliveryRating,
    String deliveryComment
) {

    public ReviewUpdateCommand {
        if (memberId == null || reviewId == null || tasteRating == null
            || amountRating == null || priceRating == null || content == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
