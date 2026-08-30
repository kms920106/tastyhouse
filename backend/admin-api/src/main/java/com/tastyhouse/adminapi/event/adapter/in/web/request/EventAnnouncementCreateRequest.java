package com.tastyhouse.adminapi.event.adapter.in.web.request;

import com.tastyhouse.adminapplication.event.port.in.EventAnnouncementCreateCommand;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "당첨자 발표 공지 등록 요청")
public record EventAnnouncementCreateRequest(
    @NotBlank(message = "발표 제목은 필수입니다.")
    @Size(max = 200, message = "발표 제목은 200자를 초과할 수 없습니다.")
    @Schema(description = "발표 제목", example = "1월 이벤트 당첨자 발표", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @NotBlank(message = "발표 내용은 필수입니다.")
    @Size(max = 1000, message = "발표 내용은 1000자를 초과할 수 없습니다.")
    @Schema(description = "발표 내용", example = "축하합니다...", requiredMode = Schema.RequiredMode.REQUIRED)
    String content,

    @NotNull(message = "발표 일시는 필수입니다.")
    @Schema(description = "발표 일시", example = "2026-02-01T10:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime announcedAt
) {

    public EventAnnouncementCreateCommand toCommand(Long eventId) {
        return new EventAnnouncementCreateCommand(eventId, name(), content(), announcedAt());
    }
}
