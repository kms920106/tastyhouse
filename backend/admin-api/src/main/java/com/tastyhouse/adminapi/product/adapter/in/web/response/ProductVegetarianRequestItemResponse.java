package com.tastyhouse.adminapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴 채식 설정 요청 검수 목록 항목.
 *
 * <p>{@code ingredients}·{@code description}을 목록에 함께 담는다 — 그것이 검수의 유일한 근거이므로
 * 검수자가 상세를 다시 열지 않고 판정할 수 있어야 한다.
 */
@Schema(description = "메뉴 채식 설정 요청 목록 항목")
public record ProductVegetarianRequestItemResponse(
    @Schema(description = "요청 ID", example = "7")
    Long id,

    @Schema(description = "메뉴 ID", example = "5")
    Long productId,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "메뉴명", example = "비빔밥")
    String productName,

    @Schema(description = "요청한 채식 단계", example = "VEGAN",
        allowableValues = {"VEGAN", "LACTO", "OVO", "LACTO_OVO", "PESCO"})
    String vegetarianType,

    @Schema(description = "채소 외 포함 재료(검수 근거)", example = "두부, 표고버섯, 간장")
    String ingredients,

    @Schema(description = "검수 참고용 메뉴 설명", example = "동물성 재료를 전혀 쓰지 않습니다.")
    String description,

    @Schema(description = "승인 상태", example = "PENDING",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELED"})
    String status,

    @Schema(description = "반려 사유. 반려가 아니면 null", example = "액젓이 포함되어 비건에 해당하지 않습니다.")
    String rejectReason
) {

    public static ProductVegetarianRequestItemResponse from(
        Long id,
        Long productId,
        Long shopId,
        String productName,
        String vegetarianType,
        String ingredients,
        String description,
        String status,
        String rejectReason
    ) {
        return new ProductVegetarianRequestItemResponse(
            id,
            productId,
            shopId,
            productName,
            vegetarianType,
            ingredients,
            description,
            status,
            rejectReason
        );
    }
}
