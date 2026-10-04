package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

public record ProductBatchQuery(
    List<Item> items,
    String orderMethod
) {

    public ProductBatchQuery {
        if (items == null || items.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
        items = List.copyOf(items);
    }

    public record Item(Long productId, Long optionId) {

        public Item {
            if (productId == null) {
                throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
            }
        }
    }
}
