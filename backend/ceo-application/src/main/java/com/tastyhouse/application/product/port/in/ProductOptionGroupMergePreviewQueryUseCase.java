package com.tastyhouse.application.product.port.in;

import java.util.List;

import com.tastyhouse.application.product.port.out.ProductOptionGroupMergePreviewResult;

public interface ProductOptionGroupMergePreviewQueryUseCase {

    ProductOptionGroupMergePreviewResult getMergePreview(
        Long ceoId,
        Long shopId,
        Long baseOptionGroupId,
        List<Long> optionGroupIds
    );
}
