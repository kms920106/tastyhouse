package com.tastyhouse.core.domain.product.application.dto.result;

import java.math.BigDecimal;
import java.util.List;

/**
 * 상품 배치 조회 결과. 상품 1개와 요청된 옵션 목록을 묶습니다.
 * 존재하지 않거나 비활성인 상품은 제외하지 않고 available=false 로 남겨, 프론트가
 * "판매 종료된 상품" 안내를 띄울 수 있게 합니다. (쿠팡 cartItemEnable 방식)
 * available=false 인 경우 name/price/options 는 비어 있습니다.
 * 요청한 옵션 중 조회에 실패하거나 해당 상품에 속하지 않는 옵션은 options 에서 제외됩니다.
 */
public record ProductBatchResult(
    Long id,
    boolean available,
    String name,
    String imageFilePath,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    List<BatchOptionResult> options
) {
}
