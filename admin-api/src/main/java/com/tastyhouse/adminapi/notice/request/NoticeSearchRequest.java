package com.tastyhouse.adminapi.notice.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "공지사항 검색 요청")
public record NoticeSearchRequest(
    @Schema(description = "제목 (부분 일치 검색)", example = "서비스 점검 안내")
    String title,

    @Schema(description = "내용 (부분 일치 검색)", example = "서비스 점검이 진행됩니다")
    String content,

    @Schema(description = "노출 여부", example = "true")
    Boolean visible
) {
}
