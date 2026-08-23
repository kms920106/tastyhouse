package com.tastyhouse.webapi.product.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 메뉴 정보에 대한 고객 의견 제보 요청.
 *
 * <p>{@code content}의 "{@code ETC}면 필수" 조건은 여기서 표현하지 않는다 — 유형에 따라 달라지는
 * 조건부 필수는 Bean Validation의 필드 단위 어노테이션으로 정확히 쓸 수 없고, 무엇보다 그 판단은
 * 도메인 불변식이라 {@code ProductFeedback}이 소유한다(HTTP 경계에만 두면 다른 호출 경로가 우회한다).
 */
@Schema(description = "메뉴 정보 고객 의견 제보 요청")
public record ProductFeedbackCreateRequest(

    @NotBlank(message = "의견 유형은 필수입니다.")
    @Schema(description = "의견 유형", example = "PRICE",
        allowableValues = {"PRICE", "IMAGE", "COMPOSITION", "SOLD_OUT", "ETC"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String feedbackType,

    @Size(max = 500, message = "의견은 500자 이내로 입력해 주세요.")
    @Schema(description = "의견 내용. 유형이 ETC이면 필수입니다", example = "메뉴 사진이 실제와 많이 달라요.")
    String content
) {
}
