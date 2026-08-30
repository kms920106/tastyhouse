package com.tastyhouse.adminapi.event.adapter.in.web.request;

import com.tastyhouse.adminapplication.event.port.in.EventCreateCommand;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "이벤트 등록 요청")
public record EventCreateRequest(
    @NotBlank(message = "이벤트명은 필수입니다.")
    @Size(max = 200, message = "이벤트명은 200자를 초과할 수 없습니다.")
    @Schema(description = "이벤트명", example = "신년 맞이 이벤트", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @Size(max = 1000, message = "이벤트 설명은 1000자를 초과할 수 없습니다.")
    @Schema(description = "이벤트 설명", example = "1월 한정 이벤트")
    String description,

    @Size(max = 200, message = "부제목은 200자를 초과할 수 없습니다.")
    @Schema(description = "부제목", example = "최대 50% 할인")
    String subtitle,

    @Schema(description = "썸네일 이미지 파일 ID", example = "10")
    Long thumbnailImageFileId,

    @Schema(description = "배너 이미지 파일 ID", example = "11")
    Long bannerImageFileId,

    @Schema(description = "본문 HTML", example = "<p>내용</p>")
    String contentHtml,

    @NotBlank(message = "이벤트 상태는 필수입니다.")
    @Schema(description = "이벤트 상태 (SCHEDULED: 예정, ACTIVE: 진행중, ENDED: 종료)", example = "SCHEDULED", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"SCHEDULED", "ACTIVE", "ENDED"})
    String status,

    @NotNull(message = "시작 일시는 필수입니다.")
    @Schema(description = "시작 일시", example = "2026-01-01T00:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime startAt,

    @NotNull(message = "종료 일시는 필수입니다.")
    @Schema(description = "종료 일시", example = "2026-01-31T23:59:59", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime endAt
) {

    public EventCreateCommand toCommand() {
        return new EventCreateCommand(
            name(),
            description(),
            subtitle(),
            thumbnailImageFileId(),
            bannerImageFileId(),
            contentHtml(),
            status(),
            startAt(),
            endAt()
        );
    }
}
