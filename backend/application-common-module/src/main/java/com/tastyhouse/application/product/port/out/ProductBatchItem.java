package com.tastyhouse.application.product.port.out;

/**
 * 배치 조회 입력 항목. (상품ID, 옵션ID) 한 조합을 나타낸다.
 *
 * <p>{@code ProductQueryPort#findProductsBatch}의 파라미터이므로 검색 조건({@code *SearchCondition})과
 * 같은 이유로 포트와 같은 패키지가 소유한다 — 입력 타입이 infra에 남으면 포트를 주입하는 소비 모듈이
 * 그 타입 때문에 여전히 infra를 import하게 되어 소유권 역전이 이름뿐이 된다.
 */
public record ProductBatchItem(
    Long productId,
    Long optionId
) {

    public static ProductBatchItem of(Long productId, Long optionId) {
        return new ProductBatchItem(productId, optionId);
    }
}
