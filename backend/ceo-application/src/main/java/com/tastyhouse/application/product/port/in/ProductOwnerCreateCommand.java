package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductOwnerCreateCommand(
    Long ceoId,
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
    List<ProductShopLinkItemCommand> links
) {

    public ProductOwnerCreateCommand {
        if (ceoId == null
            || shopId == null
            || name == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
