package com.tastyhouse.domain.product.vo;

/**
 * 상품 공통 옵션 식별자.
 *
 * <p>일반 옵션({@link ProductOptionId})과 <b>다른 테이블·다른 id 시퀀스</b>라 값이 겹칠 수 있으므로,
 * 두 갈래를 섞어 다루는 경로에서는 id만으로 대상을 특정하지 못한다. 타입을 분리해 그 혼동을 막는다.
 */
public record ProductCommonOptionId(Long value) {
    public ProductCommonOptionId {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("ProductCommonOptionId는 양수여야 합니다: " + value);
        }
    }

    public static ProductCommonOptionId of(Long value) {
        return new ProductCommonOptionId(value);
    }
}
