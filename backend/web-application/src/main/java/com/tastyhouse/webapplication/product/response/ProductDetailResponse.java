package com.tastyhouse.webapplication.product.response;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 상세 정보")
public record ProductDetailResponse(
    @Schema(description = "상품 ID", example = "1")
    Long id,

    @Schema(description = "상품명", example = "명란 크림 파스타")
    String name,

    @Schema(description = "상품 설명", example = "신선한 명란과 크림소스의 조화")
    String description,

    @Schema(description = "원가", example = "18500")
    Integer originalPrice,

    @Schema(description = "할인가", example = "16650")
    Integer discountPrice,

    @Schema(description = "할인율", example = "10.00")
    BigDecimal discountRate,

    @Schema(description = "품절 여부", example = "false")
    boolean soldOut,

    @Schema(description = "중량 표기(치킨 등 법정 의무표시 대상). 미표시면 null입니다.",
        example = "조리 전 총 중량 1,200g")
    String weightText,

    @Schema(description = "이 상품의 노출 메뉴 평가 수(숨김 제외). 평가가 없으면 0입니다.", example = "12")
    long menuReviewCount,

    @Schema(description = "가격명별 가격 목록. 각 항목의 price는 요청한 주문유형(orderMethod)으로 서버가 "
        + "이미 해석한 단일 결제 가격입니다. 가격 행이 없는 메뉴(이관 이전 데이터)는 빈 배열이며, 그때는 "
        + "originalPrice/discountPrice 기존 필드를 그대로 사용합니다.")
    List<ProductPriceResponse> prices
) {
    public static ProductDetailResponse from(
        Long id,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        boolean soldOut,
        String weightText,
        long menuReviewCount,
        List<ProductPriceResponse> prices
    ) {
        return new ProductDetailResponse(
            id,
            name,
            description,
            originalPrice,
            discountPrice,
            discountRate,
            soldOut,
            weightText,
            menuReviewCount,
            prices
        );
    }
}
