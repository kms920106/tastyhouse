package com.tastyhouse.webapi.rank.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "랭킹 기간 응답")
public record RankDurationResponse(
    @Schema(description = "시작일시", example = "2024-08-01T00:00:00")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime startAt,

    @Schema(description = "종료일시", example = "2024-08-31T23:59:59")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    LocalDateTime endAt
) {
    public static RankDurationResponse from(
        LocalDateTime startAt,
        LocalDateTime endAt
    ) {
        return new RankDurationResponse(
            startAt,
            endAt
        );
    }
}
