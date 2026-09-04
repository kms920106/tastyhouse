package com.tastyhouse.adminapi.event.adapter.in.web.request;

import com.tastyhouse.application.event.port.in.EventWinnerCreateCommand;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "당첨자 등록 요청")
public record EventWinnerCreateRequest(
    @NotNull(message = "당첨 순위는 필수입니다.")
    @Schema(description = "당첨 순위", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Integer rankNo,

    @NotBlank(message = "당첨자 이름은 필수입니다.")
    @Size(max = 50, message = "당첨자 이름은 50자를 초과할 수 없습니다.")
    @Schema(description = "당첨자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    String winnerName,

    @NotBlank(message = "휴대폰번호는 필수입니다.")
    @Pattern(regexp = "^01[0-9]{8,9}$", message = "올바른 휴대폰번호 형식이 아닙니다. (예: 01012345678)")
    @Schema(description = "당첨자 휴대폰번호 (숫자만)", example = "01012345678", requiredMode = Schema.RequiredMode.REQUIRED)
    String phoneNumber,

    @NotNull(message = "발표 일시는 필수입니다.")
    @Schema(description = "발표 일시", example = "2026-02-01T10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime announcedAt
) {

    public EventWinnerCreateCommand toCommand(Long eventId) {
        return new EventWinnerCreateCommand(eventId, rankNo(), winnerName(), phoneNumber(), announcedAt());
    }
}
