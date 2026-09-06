package com.tastyhouse.adminapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductRepresentativeRequestResult;

@Schema(description = "사장님 추천 메뉴 지정 요청 목록 항목")
public record ProductRepresentativeRequestItemResponse(
    @Schema(description = "요청 ID", example = "7")
    Long id,

    @Schema(description = "메뉴 ID", example = "5")
    Long productId,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "가게명", example = "맛있는집 강남점")
    String shopName,

    @Schema(description = "메뉴명", example = "명란 크림 파스타")
    String productName,

    @Schema(description = "메뉴 대표 이미지 URL(검수 근거). 없으면 null",
        example = "https://example.com/menu.jpg")
    String imageUrl,

    @Schema(description = "승인 상태", example = "PENDING",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELED"})
    String status,

    @Schema(description = "반려 사유. 반려가 아니면 null", example = "메뉴가 잘 보이지 않습니다.")
    String rejectReason
) {
    public static ProductRepresentativeRequestItemResponse from(ProductRepresentativeRequestResult result) {
        return new ProductRepresentativeRequestItemResponse(
            result.id(),
            result.productId(),
            result.shopId(),
            result.shopName(),
            result.productName(),
            result.imageUrl(),
            result.status().name(),
            result.rejectReason()
        );
    }
}
