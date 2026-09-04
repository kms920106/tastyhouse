package com.tastyhouse.webapi.product.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductBatchItemView;

@Schema(description = "배치 조회 상품")
public record ProductResponse(
    @Schema(description = "상품 ID", example = "1")
    Long id,

    @Schema(description = "구매 가능 여부. 판매 종료/미존재 상품이면 false 이고 이하 필드는 비어 있음", example = "true")
    boolean available,

    @Schema(description = "상품명 (available=false 면 null)", example = "후라이드 치킨", nullable = true)
    String name,

    @Schema(description = "상품 대표 이미지 URL (available=false 이거나 이미지가 없으면 null)", example = "https://cdn.example.com/products/1.jpg", nullable = true)
    String imageUrl,

    @Schema(description = "정가 (available=false 면 null)", example = "18000", nullable = true)
    Integer originalPrice,

    @Schema(description = "할인가. 할인이 없거나 available=false 면 null", example = "16000", nullable = true)
    Integer discountPrice,

    @Schema(description = "요청한 옵션 중 조회에 성공한 옵션 목록 (available=false 면 빈 배열)")
    List<ProductBatchOptionResponse> options,

    @Schema(description = "가격 행 목록. 장바구니가 보관한 priceId로 가격명·가격을 되찾는 데 쓴다. "
        + "각 price는 요청한 orderMethod로 서버가 이미 해석한 단일 결제 가격이다. "
        + "가격 행이 없는 메뉴(이관 이전 데이터)와 available=false 면 빈 배열")
    List<ProductPriceResponse> prices
) {
    public static ProductResponse from(ProductBatchItemView view) {
        return new ProductResponse(
            view.id(),
            view.available(),
            view.name(),
            view.imageUrl(),
            view.originalPrice(),
            view.discountPrice(),
            view.options().stream().map(ProductBatchOptionResponse::from).toList(),
            view.prices().stream().map(ProductPriceResponse::from).toList()
        );
    }
}
