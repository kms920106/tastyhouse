package com.tastyhouse.application.product.port.out.write;

public record ProductOptionGroupMergeExclusionState(
    Long id,
    Long shopId,
    String groupSignature,
    Long actorCeoId
) {
}
