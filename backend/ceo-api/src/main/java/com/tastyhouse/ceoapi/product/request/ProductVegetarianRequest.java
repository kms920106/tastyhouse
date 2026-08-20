package com.tastyhouse.ceoapi.product.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴 채식 설정 요청.
 *
 * <p>{@code ingredients}가 필수인 이유는 그것이 관리자 검수의 유일한 근거이기 때문이다 — 재료를
 * 보지 않고는 이 메뉴가 정말 그 채식 단계인지 판정할 수 없다.
 */
@Schema(description = "메뉴 채식 설정 요청")
public record ProductVegetarianRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotBlank(message = "채식 단계는 필수입니다.")
    @Schema(description = "채식 단계", example = "VEGAN",
        allowableValues = {"VEGAN", "LACTO", "OVO", "LACTO_OVO", "PESCO"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String vegetarianType,

    @NotBlank(message = "포함 재료는 필수입니다.")
    @Size(max = 1000, message = "포함 재료는 1000자 이하여야 합니다.")
    @Schema(description = "채소 외 포함 재료. 검수의 근거이므로 빠짐없이 적는다.", example = "두부, 표고버섯, 간장",
        requiredMode = Schema.RequiredMode.REQUIRED)
    String ingredients,

    @Size(max = 1000, message = "메뉴 설명은 1000자 이하여야 합니다.")
    @Schema(description = "검수 참고용 메뉴 설명", example = "동물성 재료를 전혀 쓰지 않는 비건 비빔밥입니다.")
    String description
) {
}
