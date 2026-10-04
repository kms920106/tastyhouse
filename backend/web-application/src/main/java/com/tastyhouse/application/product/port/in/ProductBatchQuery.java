package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record ProductBatchQuery(
    List<Item> items,
    String orderMethod
) {

    public ProductBatchQuery {
        if (items == null || items.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        items = List.copyOf(items);
    }

    public record Item(Long productId, Long optionId) {

        public Item {
            if (productId == null) {
                throw new BusinessException(ErrorCode.INVALID_INPUT);
            }
        }
    }
}
