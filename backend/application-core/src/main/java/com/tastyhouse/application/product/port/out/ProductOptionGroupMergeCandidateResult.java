package com.tastyhouse.application.product.port.out;

public record ProductOptionGroupMergeCandidateResult(
    Long optionGroupId,
    String name,
    Integer minSelect,
    Integer maxSelect,
    String signaturePayload
) {
}
