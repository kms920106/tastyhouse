package com.tastyhouse.adminapi.event.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.event.port.out.EventAnnouncementResult;

@Schema(description = "당첨자 발표 공지 응답")
public record EventAnnouncementResponse(
    @Schema(description = "공지 ID", example = "5")
    Long id,

    @Schema(description = "이벤트 ID", example = "1")
    Long eventId,

    @Schema(description = "발표 제목", example = "1월 이벤트 당첨자 발표")
    String name,

    @Schema(description = "발표 내용", example = "축하합니다...")
    String content,

    @Schema(description = "발표 일시", example = "2026-02-01T10:00:00")
    LocalDateTime announcedAt
) {
    public static EventAnnouncementResponse from(EventAnnouncementResult result) {
        return new EventAnnouncementResponse(
            result.id(),
            result.eventId(),
            result.name(),
            result.content(),
            result.announcedAt()
        );
    }
}
