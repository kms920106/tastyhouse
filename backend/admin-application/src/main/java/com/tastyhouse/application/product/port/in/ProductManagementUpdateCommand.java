package com.tastyhouse.application.product.port.in;

import java.math.BigDecimal;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductManagementUpdateCommand(
    Long productId,
    Long productCategoryId,
    String name,
    String description,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    Boolean representative,
    Integer spiciness,
    Boolean soldOut,
    Boolean visible,
    Integer sort
) {

    public ProductManagementUpdateCommand {
        if (productId == null || name == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
