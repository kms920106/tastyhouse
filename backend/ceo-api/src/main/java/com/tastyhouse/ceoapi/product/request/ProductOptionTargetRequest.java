package com.tastyhouse.ceoapi.product.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 옵션 일괄 처리의 대상 지정 요소.
 *
 * <p>{@code optionType}이 함께 필요한 이유: 일반 옵션과 공통 옵션은 <b>다른 테이블·다른 id 시퀀스</b>라
 * id만으로는 어느 쪽인지 알 수 없다. 이 값이 있어야 서버가 올바른 리포지토리를 고를 수 있다.
 */
@Schema(description = "옵션 일괄 처리 대상")
public record ProductOptionTargetRequest(
    @NotNull(message = "옵션 ID는 필수입니다.")
    @Schema(description = "대상 옵션 ID", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    Long optionId,

    @NotBlank(message = "옵션 종류는 필수입니다.")
    @Schema(description = "옵션 종류. NORMAL은 일반 옵션, COMMON은 공통 옵션이다.", example = "NORMAL",
        allowableValues = {"NORMAL", "COMMON"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String optionType
) {
}
