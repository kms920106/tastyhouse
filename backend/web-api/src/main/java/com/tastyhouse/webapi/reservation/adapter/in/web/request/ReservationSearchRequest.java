package com.tastyhouse.webapi.reservation.adapter.in.web.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

@Schema(description = "슬롯 가용성 조회 요청")
public record ReservationSearchRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotNull(message = "조회 날짜는 필수입니다.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "조회 날짜 (yyyy-MM-dd)", example = "2026-07-12", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDate date
) {
}
