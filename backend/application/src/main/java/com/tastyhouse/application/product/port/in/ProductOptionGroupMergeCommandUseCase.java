package com.tastyhouse.application.product.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;

@CeoApp
public interface ProductOptionGroupMergeCommandUseCase {

    Long mergeProductOptionGroups(ProductOptionGroupMergeCommand command);

    Long excludeMergeSuggestion(ProductOptionGroupMergeExclusionCreateCommand command);
}
