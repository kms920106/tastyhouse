package com.tastyhouse.application.product.port.out.write;

public record ProductOptionGroupMergeHistoryState(
    Long id,
    Long shopId,
    Long baseOptionGroupId,
    Long mergedOptionGroupId,
    String mergedGroupName,
    String entryType,
    Long actorCeoId
) {
}
