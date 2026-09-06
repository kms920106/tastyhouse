package com.tastyhouse.ceoapi.region.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.region.port.out.AdminDongTreeResult;

@Schema(description = "행정동 계층 조회 결과")
public record AdminDongTreeResponse(
    @Schema(description = "계층 단계", example = "SIGUNGU", allowableValues = {"SIDO", "SIGUNGU", "DONG"})
    String level,

    @Schema(description = "해당 단계의 항목 목록")
    List<AdminDongTreeItemResponse> items
) {
    public static AdminDongTreeResponse from(AdminDongTreeResult result) {
        return new AdminDongTreeResponse(
            result.level(),
            result.items().stream().map(AdminDongTreeItemResponse::from).toList()
        );
    }
}
