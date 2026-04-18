package com.tastyhouse.webapi.event.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "이벤트 당첨자 발표 응답")
public record EventWinnerListResponse(
    @Schema(description = "이벤트 ID", example = "1")
    Long eventId,

    @Schema(description = "이벤트명", example = "8월 신규회원 특별 할인 이벤트 당첨자 발표")
    String eventName,

    @Schema(description = "발표일시", example = "2020-09-10T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime announcedAt,

    @Schema(description = "당첨자 목록")
    List<EventWinnerItemResponse> winners
) {
}
