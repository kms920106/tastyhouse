package com.tastyhouse.adminapi.rank.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "회원 랭킹 조회 요청")
public record RankSearchRequest(
    @Schema(description = "랭킹 타입", allowableValues = {"ALL", "MONTHLY", "WEEKLY"}, example = "MONTHLY")
    String type,

    @Schema(description = "조회할 랭킹 개수", example = "100")
    Integer limit
) {

    public RankSearchRequest {
        if (type == null) {
            type = "ALL";
        }
        if (limit == null) {
            limit = 100;
        }
    }
}
