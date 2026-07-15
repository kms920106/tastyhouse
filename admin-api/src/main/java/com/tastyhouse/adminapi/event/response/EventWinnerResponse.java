package com.tastyhouse.adminapi.event.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.core.domain.event.application.dto.EventWinnerDto;

@Schema(description = "당첨자 응답")
public record EventWinnerResponse(
    @Schema(description = "당첨자 ID", example = "100")
    Long id,

    @Schema(description = "이벤트 ID", example = "1")
    Long eventId,

    @Schema(description = "당첨 순위", example = "1")
    Integer rankNo,

    @Schema(description = "당첨자 이름", example = "홍길동")
    String winnerName,

    @Schema(description = "당첨자 휴대폰번호", example = "01012345678")
    String phoneNumber,

    @Schema(description = "발표 일시", example = "2026-02-01T10:00:00")
    LocalDateTime announcedAt
) {
    public static EventWinnerResponse from(EventWinnerDto dto) {
        return new EventWinnerResponse(
            dto.id(),
            dto.eventId(),
            dto.rankNo(),
            dto.winnerName(),
            dto.phoneNumber(),
            dto.announcedAt()
        );
    }
}
