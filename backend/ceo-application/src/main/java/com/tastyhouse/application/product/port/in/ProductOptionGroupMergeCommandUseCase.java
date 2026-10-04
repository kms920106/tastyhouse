package com.tastyhouse.application.product.port.in;

public interface ProductOptionGroupMergeCommandUseCase {

    Long mergeProductOptionGroups(ProductOptionGroupMergeCommand command);

    Long excludeMergeSuggestion(ProductOptionGroupMergeExclusionCreateCommand command);
}
