package com.tastyhouse.webapplication.event.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이벤트 목록 아이템 응답")
public record EventListItemResponse(
    @Schema(description = "이벤트 ID", example = "1")
    Long id,

    @Schema(description = "이벤트명", example = "8월 신규회원 특별 할인 이벤트")
    String name,

    @Schema(description = "썸네일 이미지 URL", example = "https://example.com/thumbnail.jpg")
    String thumbnailImageUrl,

    @Schema(description = "이벤트 시작일시", example = "2020-08-03T00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startAt,

    @Schema(description = "이벤트 종료일시", example = "2020-08-31T23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime endAt
) {
    public static EventListItemResponse from(
        Long id,
        String name,
        String thumbnailImageUrl,
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        return new EventListItemResponse(
            id,
            name,
            thumbnailImageUrl,
            startAt,
            endAt
        );
    }
}
