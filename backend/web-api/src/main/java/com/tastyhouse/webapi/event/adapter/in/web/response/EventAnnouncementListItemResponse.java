package com.tastyhouse.webapi.event.adapter.in.web.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.event.port.out.EventAnnouncementResult;

@Schema(description = "이벤트 당첨자 발표 목록 아이템")
public record EventAnnouncementListItemResponse(
    @Schema(description = "발표 ID", example = "1")
    Long id,

    @Schema(description = "발표명", example = "8월 신규회원 특별 할인 이벤트 당첨자 발표")
    String name,

    @Schema(description = "발표 HTML 콘텐츠")
    String content,

    @Schema(description = "발표일시", example = "2020-09-10T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime announcedAt
) {
    public static EventAnnouncementListItemResponse from(EventAnnouncementResult result) {
        return new EventAnnouncementListItemResponse(
            result.id(),
            result.name(),
            result.content(),
            result.announcedAt()
        );
    }
}
