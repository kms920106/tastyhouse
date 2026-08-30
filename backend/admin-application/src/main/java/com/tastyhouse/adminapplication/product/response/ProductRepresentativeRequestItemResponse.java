package com.tastyhouse.adminapplication.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사장님 추천(대표 메뉴) 지정 요청 검수 목록 항목.
 *
 * <p>{@code imageUrl}을 목록에 함께 담는다 — 대표 메뉴는 가게 상단에 사진으로 노출되므로 사진이
 * 검수의 실질적 근거이고, 검수자가 상세를 다시 열지 않고 판정할 수 있어야 한다(채식 요청이 재료를
 * 목록에 담는 것과 같은 이유).
 */
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

    public static ProductRepresentativeRequestItemResponse from(
        Long id,
        Long productId,
        Long shopId,
        String shopName,
        String productName,
        String imageUrl,
        String status,
        String rejectReason
    ) {
        return new ProductRepresentativeRequestItemResponse(
            id,
            productId,
            shopId,
            shopName,
            productName,
            imageUrl,
            status,
            rejectReason
        );
    }
}
