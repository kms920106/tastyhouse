package com.tastyhouse.adminapi.event.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.event.port.out.EventManagementDetailResult;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

import com.tastyhouse.adminapi.common.response.FileResponse;

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
    public static EventDetailResponse from(EventManagementDetailResult result) {
        return new EventDetailResponse(
            result.id(),
            result.name(),
            result.description(),
            result.subtitle(),
            toFileResponse(result.thumbnailImageFileId(), result.thumbnailFileName(), result.thumbnailUrl()),
            toFileResponse(result.bannerImageFileId(), result.bannerFileName(), result.bannerUrl()),
            result.contentHtml(),
            result.status().name(),
            result.startAt(),
            result.endAt(),
            result.createdAt(),
            result.updatedAt()
        );
    }

    /**
     * 상세용 — DAO가 join으로 함께 가져온 파일명·URL로 조립한다(추가 조회 없음). fileId가 없으면(파일
     * 미등록) {@code null}을 그대로 반환하되, fileId는 있는데 left join이 URL을 못 찾았다면(참조 무결성
     * 깨짐) 과거 {@code fileService.findFileResponse} 호출 시의 {@code FILE_NOT_FOUND} 동작을 그대로
     * 보존한다 — 썸네일·배너는 필수 자산이므로 조용히 null을 내려보내지 않는다.
     */
    private static FileResponse toFileResponse(Long fileId, String fileName, String imageUrl) {
        if (fileId == null) {
            return null;
        }
        if (imageUrl == null) {
            throw new ResourceNotFoundException(ErrorCode.FILE_NOT_FOUND);
        }
        return FileResponse.of(fileId, fileName, imageUrl);
    }
}
