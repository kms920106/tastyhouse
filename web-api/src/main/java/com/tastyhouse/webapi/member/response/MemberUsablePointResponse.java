package com.tastyhouse.webapi.member.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "사용 가능 포인트 응답 DTO (주문용)")
public record MemberUsablePointResponse(
    @Schema(description = "사용 가능 포인트", example = "1000")
    Integer usablePoints
) {
    public static MemberUsablePointResponse of(Integer usablePoints) {
        return new MemberUsablePointResponse(usablePoints);
    }
}
