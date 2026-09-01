package com.tastyhouse.adminapi.event.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.event.port.out.EventManagementListItemResult;

import com.tastyhouse.adminapi.common.response.FileResponse;

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
    public static EventListItemResponse from(EventManagementListItemResult result) {
        return new EventListItemResponse(
            result.id(),
            result.name(),
            result.status().name(),
            toFileResponse(result.thumbnailImageFileId(), result.thumbnailFileName(), result.thumbnailUrl()),
            result.startAt(),
            result.endAt()
        );
    }

    /**
     * 목록용 — DAO가 join으로 함께 가져온 파일명·URL로 조립한다(추가 조회 없음). fileId가 없으면(파일
     * 미등록) {@code null}을 그대로 반환한다.
     */
    private static FileResponse toFileResponse(Long fileId, String fileName, String imageUrl) {
        if (fileId == null) {
            return null;
        }
        return FileResponse.of(fileId, fileName, imageUrl);
    }
}
