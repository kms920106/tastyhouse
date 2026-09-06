package com.tastyhouse.ceoapi.region.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.region.port.out.AdminDongBoundariesResult;

@Schema(description = "행정동 경계 조회 결과")
public record AdminDongBoundaryResponse(
    @Schema(description = "조회 영역이 너무 넓어 경계를 생략했는지", example = "false")
    boolean truncated,

    @Schema(description = "행정동 경계 목록")
    List<AdminDongBoundaryItemResponse> items
) {
    public static AdminDongBoundaryResponse from(AdminDongBoundariesResult result) {
        return new AdminDongBoundaryResponse(
            result.truncated(),
            result.items().stream().map(AdminDongBoundaryItemResponse::from).toList()
        );
    }
}
