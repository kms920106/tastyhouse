package com.tastyhouse.ceoapi.region.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.region.port.out.AdminDongTreeItemResult;

@Schema(description = "행정동 계층 항목 한 건")
public record AdminDongTreeItemResponse(
    @Schema(description = "표시명", example = "강남구")
    String name,

    @Schema(description = "행정동 ID(DONG 레벨에서만 채워짐)", example = "1101053")
    Long adminDongId,

    @Schema(description = "행정동 코드(DONG 레벨에서만 채워짐)", example = "1168053100")
    String code,

    @Schema(description = "하위 행정동 수(DONG 레벨에서는 1)", example = "22")
    long dongCount
) {
    public static AdminDongTreeItemResponse from(AdminDongTreeItemResult result) {
        return new AdminDongTreeItemResponse(
            result.name(),
            result.adminDongId(),
            result.code(),
            result.dongCount()
        );
    }
}
