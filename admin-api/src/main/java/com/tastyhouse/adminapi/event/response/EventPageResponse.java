package com.tastyhouse.adminapi.event.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.shared.page.PageResult;

@Schema(description = "이벤트 목록 페이지 응답")
public record EventPageResponse(
    @Schema(description = "이벤트 목록")
    List<EventListItemResponse> content,

    @Schema(description = "페이지 번호(0부터 시작)", example = "0")
    int page,

    @Schema(description = "페이지 크기", example = "10")
    int size,

    @Schema(description = "전체 요소 수", example = "42")
    long totalElements
) {

    public static EventPageResponse from(PageResult<EventListItemResponse> pageResult) {
        return new EventPageResponse(pageResult.content(), pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
