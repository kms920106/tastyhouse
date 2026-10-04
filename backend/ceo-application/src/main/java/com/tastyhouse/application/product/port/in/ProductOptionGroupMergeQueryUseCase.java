package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductOptionGroupMergePreviewResult;
import com.tastyhouse.application.product.port.out.ProductOptionGroupMergeSuggestionResult;

public interface ProductOptionGroupMergeQueryUseCase {

    List<ProductOptionGroupMergeSuggestionResult> getMergeSuggestions(Long ceoId, Long shopId);

    ProductOptionGroupMergePreviewResult getMergePreview(
        Long ceoId,
        Long shopId,
        Long baseOptionGroupId,
        List<Long> optionGroupIds
    );
}
