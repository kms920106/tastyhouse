package com.tastyhouse.webapi.event.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "이벤트 목록 조회 요청")
public record EventSearchRequest(
    @NotBlank(message = "이벤트 상태는 필수입니다.")
    @Schema(description = "이벤트 상태 (SCHEDULED: 예정, ACTIVE: 진행중, ENDED: 종료)", example = "ACTIVE", allowableValues = {"SCHEDULED", "ACTIVE", "ENDED"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String status
) {
}
