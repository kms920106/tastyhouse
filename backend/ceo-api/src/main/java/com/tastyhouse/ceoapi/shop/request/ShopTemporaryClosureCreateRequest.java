package com.tastyhouse.ceoapi.shop.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "가게 임시 휴무 등록 요청")
public record ShopTemporaryClosureCreateRequest(
    @NotNull(message = "시작일은 필수입니다.")
    @Schema(description = "임시 휴무 시작일", example = "2026-08-01", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDate startDate,

    @NotNull(message = "종료일은 필수입니다.")
    @Schema(description = "임시 휴무 종료일", example = "2026-08-03", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDate endDate
) {
}
