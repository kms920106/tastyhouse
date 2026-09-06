package com.tastyhouse.application.product.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductOwnerUpdateCommand(
    Long ceoId,
    Long productId,
    Long shopId,
    Long productCategoryId,
    String name,
    String composition,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    Boolean singleServing,
    Integer spiciness,
    Boolean representative,
    Boolean ratingExcluded,
    String weightText
) {
    public ProductOwnerUpdateCommand {
        if (ceoId == null
            || productId == null
            || shopId == null
            || name == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
