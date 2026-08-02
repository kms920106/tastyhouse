package com.tastyhouse.infrastructure.product.query;

/**
 * 배치 조회 입력 항목. (상품ID, 옵션ID) 한 조합을 나타낸다.
 */
public record ProductBatchItem(
    Long productId,
    Long optionId
) {

    public static ProductBatchItem of(Long productId, Long optionId) {
        return new ProductBatchItem(productId, optionId);
    }
}
