package com.tastyhouse.webapi.reservation.adapter.in.web.request;

import java.time.LocalDate;
import java.time.LocalTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.webapi.reservation.application.port.in.ReservationCreateCommand;

@Schema(description = "예약 생성 요청")
public record ReservationCreateRequest(

    @NotNull(message = "가게 ID는 필수입니다")
    @Schema(description = "가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @NotNull(message = "예약 날짜는 필수입니다")
    @FutureOrPresent(message = "예약 날짜는 오늘 이후여야 합니다")
    @Schema(description = "예약 날짜", example = "2026-06-10", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDate reservationDate,

    @NotNull(message = "예약 시간은 필수입니다")
    @Schema(description = "예약 시간 (30분 단위, 10:30~19:30)", example = "13:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalTime reservationTime,

    @NotNull(message = "인원수는 필수입니다")
    @Min(value = 1, message = "인원수는 1명 이상이어야 합니다")
    @Max(value = 50, message = "인원수는 50명을 초과할 수 없습니다")
    @Schema(description = "방문 인원수", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer partySize,

    @Schema(description = "요청사항", example = "창가 자리 부탁드립니다")
    String request,

    @AssertTrue(message = "필수 약관에 동의해야 예약할 수 있습니다")
    @Schema(description = "필수 약관 동의 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    boolean agreedRequiredTerms
) {

    /**
     * 인증 주체의 {@code memberId}를 주입받아 command로 변환한다. 각 값은 이름 기반 접근자로 짚어 넘긴다.
     */
    public ReservationCreateCommand toCommand(Long memberId) {
        return new ReservationCreateCommand(
            memberId,
            shopId,
            reservationDate,
            reservationTime,
            partySize,
            request,
            agreedRequiredTerms
        );
    }
}
