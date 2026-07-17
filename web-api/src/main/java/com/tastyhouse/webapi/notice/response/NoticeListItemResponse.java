package com.tastyhouse.webapi.notice.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.notice.application.dto.result.NoticeListItemResult;

@Schema(description = "공지사항 목록 항목 응답")
public record NoticeListItemResponse(
    @Schema(description = "공지사항 ID", example = "1")
    Long id,

    @Schema(description = "제목", example = "서비스 점검 안내")
    String title,

    @Schema(description = "공지사항 본문 내용", example = "2026년 1월 1일 00시부터 02시까지 서비스 점검이 진행됩니다.")
    String content,

    @Schema(description = "생성 일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt
) {
    public static NoticeListItemResponse from(NoticeListItemResult dto) {
        return new NoticeListItemResponse(
            dto.id(),
            dto.title(),
            dto.content(),
            dto.createdAt()
        );
    }
}
