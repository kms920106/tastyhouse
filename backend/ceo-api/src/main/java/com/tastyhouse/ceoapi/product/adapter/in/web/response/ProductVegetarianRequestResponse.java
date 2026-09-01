package com.tastyhouse.ceoapi.product.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.product.port.out.ProductVegetarianRequestResult;

/**
 * 메뉴 채식 설정 요청의 검수 상태.
 */
@Schema(description = "메뉴 채식 설정 요청")
public record ProductVegetarianRequestResponse(
    @Schema(description = "요청 ID", example = "7")
    Long id,

    @Schema(description = "요청한 채식 단계", example = "VEGAN",
        allowableValues = {"VEGAN", "LACTO", "OVO", "LACTO_OVO", "PESCO"})
    String vegetarianType,

    @Schema(description = "채소 외 포함 재료", example = "두부, 표고버섯, 간장")
    String ingredients,

    @Schema(description = "검수 참고용 메뉴 설명", example = "동물성 재료를 전혀 쓰지 않습니다.")
    String description,

    @Schema(description = "검수 상태", example = "PENDING",
        allowableValues = {"PENDING", "APPROVED", "REJECTED", "CANCELED"})
    String status,

    @Schema(description = "반려 사유. 반려가 아니면 null", example = "액젓이 포함되어 비건에 해당하지 않습니다.")
    String rejectReason
) {

    public static ProductVegetarianRequestResponse from(ProductVegetarianRequestResult result) {
        return new ProductVegetarianRequestResponse(
            result.id(),
            result.vegetarianType().name(),
            result.ingredients(),
            result.description(),
            result.status().name(),
            result.rejectReason()
        );
    }
}
