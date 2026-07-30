package com.tastyhouse.infrastructure.product.query;

import com.querydsl.core.annotations.QueryProjection;

/**
 * BBQ 옵션 동기화가 필요한 상품 read model(batch 소비). 동기화에 필요한 상품·BBQ 메뉴 식별자와
 * 기본 옵션 생성에 쓰이는 상품명만 담는다.
 */
public record ProductBbqSyncTargetResult(
    Long productId,
    Long bbqMenuId,
    String productName
) {
    @QueryProjection
    public ProductBbqSyncTargetResult {
    }
}
