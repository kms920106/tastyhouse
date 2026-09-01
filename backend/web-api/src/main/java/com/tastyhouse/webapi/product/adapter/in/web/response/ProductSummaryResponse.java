package com.tastyhouse.webapi.product.adapter.in.web.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.product.port.out.ShopProductItemResult;

/**
 * 상품 요약 응답 — 통합검색 메뉴 탭과 가게 상세의 메뉴 목록이 함께 쓴다.
 *
 * <p><b>{@code from} 오버로드가 두 벌인 이유</b>: 두 화면의 읽기 계약이 서로 다른 포트에서 오지만
 * ({@code SearchProductItemResult}는 가게명을, {@code ShopProductItemResult}는 카테고리·품절 여부를
 * 각각 더 갖는다) 이 응답이 쓰는 10개 필드는 두 계약에 모두 있다. 계약을 하나로 합치면 한쪽 화면이
 * 쓰지 않는 필드를 그 쿼리가 함께 투영해야 하므로(읽기 계약은 소비자가 실제 쓰는 필드만 담는다),
 * 계약은 그대로 두고 복사 지점만 둘로 나눈다.
 */
@Schema(description = "상품 요약 응답")
public record ProductSummaryResponse(
    @Schema(description = "상품 ID", example = "1")
    Long id,

    @Schema(description = "상품명", example = "명란 크림 파스타")
    String name,

    @Schema(description = "이미지 URL", example = "https://example.com/menu.jpg")
    String imageUrl,

    @Schema(description = "원가", example = "18500")
    Integer originalPrice,

    @Schema(description = "할인가", example = "18000")
    Integer discountPrice,

    @Schema(description = "할인율", example = "10")
    BigDecimal discountRate,

    @Schema(description = "상품 평점", example = "3.5")
    Double rating,

    @Schema(description = "리뷰 수", example = "24")
    Integer reviewCount,

    @Schema(description = "대표 상품 여부", example = "true")
    boolean representative,

    @Schema(description = "매운맛 정도 (0-5 또는 0-10)", example = "3")
    Integer spiciness
) {
    public static ProductSummaryResponse from(SearchProductItemResult result) {
        return new ProductSummaryResponse(
            result.id(),
            result.name(),
            result.imageUrl(),
            result.originalPrice(),
            result.discountPrice(),
            result.discountRate(),
            result.rating(),
            result.reviewCount(),
            result.representative(),
            result.spiciness()
        );
    }

    /**
     * 가게 상세의 카테고리별 메뉴 묶음({@code ShopProductCategoryResponse})이 쓰는 오버로드 — 검색 결과와
     * 필드 구성이 같아 같은 응답 계약으로 내려간다. 두 read model이 별도 record인 것은 조회 조건이
     * 다르기 때문이고({@code ShopProductItemResult}는 카테고리 그룹핑용 {@code productCategoryId}와
     * 품절 여부를 더 갖는다), 이 응답이 쓰는 열 필드는 동일하다.
     */
    public static ProductSummaryResponse from(ShopProductItemResult result) {
        return new ProductSummaryResponse(
            result.id(),
            result.name(),
            result.imageUrl(),
            result.originalPrice(),
            result.discountPrice(),
            result.discountRate(),
            result.rating(),
            result.reviewCount(),
            result.representative(),
            result.spiciness()
        );
    }
}
