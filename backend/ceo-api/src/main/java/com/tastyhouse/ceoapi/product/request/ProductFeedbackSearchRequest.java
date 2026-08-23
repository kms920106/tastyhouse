package com.tastyhouse.ceoapi.product.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * 점주 고객 의견 목록 조회 조건.
 *
 * <p>{@code ProductShopScopeRequest}에 페이징을 더한 형태다 — 두 record를 합치지 않는 이유는 record가
 * 상속을 지원하지 않고, 조회 조건마다 필요한 필드가 달라 하나로 묶으면 쓰지 않는 필드가 섞이기 때문이다.
 *
 * <p>조회 범위(지난 7일)는 요청 파라미터로 받지 않는다 — 점주가 창을 넓힐 수 있으면 중복 제보 방지
 * 기간과 어긋나 집계가 왜곡된다. 서버가 고정한다.
 */
@Schema(description = "점주 고객 의견 목록 조회 조건")
public record ProductFeedbackSearchRequest(

    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    @Schema(description = "페이지 번호(0부터 시작)", example = "0")
    Integer page,

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    Integer size
) {

    /**
     * 페이징 기본값을 보정한다. {@code page}/{@code size}를 프리미티브 {@code int}가 아니라
     * {@code Integer}로 두는 이유는, {@code @ModelAttribute} 바인딩이 쿼리파라미터 자체가 없을 때
     * 프리미티브에는 {@code null}을 주입하려다 {@code MethodArgumentTypeMismatchException}(400)을
     * 던지기 때문이다 — 그 경우 이 compact constructor에 도달하지 못해 기본값 보정이 무의미해진다.
     * 참조타입은 바인딩 실패 없이 {@code null}이 그대로 들어오므로, 여기서 기본값(0/10)으로 보정한다.
     */
    public ProductFeedbackSearchRequest {
        if (page == null) {
            page = 0;
        }
        if (size == null || size <= 0) {
            size = 10;
        }
    }
}
