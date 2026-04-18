package com.tastyhouse.webapi.event.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "이벤트 당첨자 아이템 응답")
public record EventWinnerItemResponse(
    @Schema(description = "당첨자 ID", example = "1")
    Long id,

    @Schema(description = "이벤트 ID", example = "1")
    Long eventId,

    @Schema(description = "순위", example = "1")
    Integer rankNo,

    @Schema(description = "마스킹된 당첨자 이름", example = "홍*동")
    String winnerName,

    @Schema(description = "마스킹된 전화번호", example = "010-****-1234")
    String phoneNumber,

    @Schema(description = "발표일시", example = "2020-09-10T10:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime announcedAt
) {
}
