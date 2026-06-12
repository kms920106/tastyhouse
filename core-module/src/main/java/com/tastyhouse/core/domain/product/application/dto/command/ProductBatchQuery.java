package com.tastyhouse.core.domain.product.application.dto.command;

import java.util.List;

/**
 * 상품 배치 조회 입력. (상품ID, 옵션ID) 조합의 목록입니다.
 * 같은 productId 에 여러 optionId 가 올 수 있으며, 응답에서는 상품 단위로 그룹핑됩니다.
 */
public record ProductBatchQuery(
    List<BatchItem> items
) {
    public record BatchItem(
        Long productId,
        Long optionId
    ) {}
}
