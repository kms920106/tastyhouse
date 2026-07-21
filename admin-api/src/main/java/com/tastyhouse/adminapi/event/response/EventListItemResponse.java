package com.tastyhouse.adminapi.event.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.adminapi.file.response.FileResponse;

@Schema(description = "이벤트 목록 항목 응답")
public record EventListItemResponse(
    @Schema(description = "이벤트 ID", example = "1")
    Long id,

    @Schema(description = "이벤트명", example = "신년 맞이 이벤트")
    String name,

    @Schema(description = "이벤트 상태 (SCHEDULED: 예정, ACTIVE: 진행중, ENDED: 종료)", example = "ACTIVE")
    String status,

    @Schema(description = "썸네일 파일 정보 (미등록 시 null)")
    FileResponse file,

    @Schema(description = "시작 일시", example = "2026-01-01T00:00:00")
    LocalDateTime startAt,

    @Schema(description = "종료 일시", example = "2026-01-31T23:59:59")
    LocalDateTime endAt
) {
    public static EventListItemResponse from(
        Long id,
        String name,
        String status,
        FileResponse file,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        return new EventListItemResponse(
            id,
            name,
            status,
            file,
            startAt,
            endAt
        );
    }
}
