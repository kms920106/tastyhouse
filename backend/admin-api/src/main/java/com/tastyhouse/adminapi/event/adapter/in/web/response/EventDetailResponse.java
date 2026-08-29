package com.tastyhouse.adminapi.event.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.adminapi.file.response.FileResponse;

@Schema(description = "이벤트 상세 응답")
public record EventDetailResponse(
    @Schema(description = "이벤트 ID", example = "1")
    Long id,

    @Schema(description = "이벤트명", example = "신년 맞이 이벤트")
    String name,

    @Schema(description = "이벤트 설명", example = "1월 한정 이벤트")
    String description,

    @Schema(description = "부제목", example = "최대 50% 할인")
    String subtitle,

    @Schema(description = "썸네일 파일 정보 (미등록 시 null)")
    FileResponse thumbnailFile,

    @Schema(description = "배너 파일 정보 (미등록 시 null)")
    FileResponse bannerFile,

    @Schema(description = "본문 HTML", example = "<p>내용</p>")
    String contentHtml,

    @Schema(description = "이벤트 상태 (SCHEDULED: 예정, ACTIVE: 진행중, ENDED: 종료)", example = "ACTIVE")
    String status,

    @Schema(description = "시작 일시", example = "2026-01-01T00:00:00")
    LocalDateTime startAt,

    @Schema(description = "종료 일시", example = "2026-01-31T23:59:59")
    LocalDateTime endAt,

    @Schema(description = "생성일시", example = "2025-12-20T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정일시", example = "2025-12-25T14:00:00")
    LocalDateTime updatedAt
) {
    public static EventDetailResponse from(
        Long id,
        String name,
        String description,
        String subtitle,
        FileResponse thumbnailFile,
        FileResponse bannerFile,
        String contentHtml,
        String status,
        LocalDateTime startAt,
        LocalDateTime endAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new EventDetailResponse(
            id,
            name,
            description,
            subtitle,
            thumbnailFile,
            bannerFile,
            contentHtml,
            status,
            startAt,
            endAt,
            createdAt,
            updatedAt
        );
    }
}
