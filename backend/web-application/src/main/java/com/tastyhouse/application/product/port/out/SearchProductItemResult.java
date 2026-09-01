package com.tastyhouse.application.product.port.out;

import java.math.BigDecimal;

/**
 * 통합검색 상품 항목 read model.
 *
 * <p>이미지는 파일 조인으로 얻은 경로를 DAO가 표시용 URL까지 변환해 담으므로, 소비 모듈은 이 값을
 * 그대로 응답에 전달한다.
 */
public record SearchProductItemResult(
    Long id,
    String shopName,
    String name,
    String imageUrl,
    Integer originalPrice,
    Integer discountPrice,
    BigDecimal discountRate,
    Double rating,
    Integer reviewCount,
    boolean representative,
    Integer spiciness
) {
}
