package com.tastyhouse.adminapi.ceo.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.ceo.port.out.CeoListItemResult;

@Schema(description = "점주 목록 항목 응답")
public record CeoListItemResponse(
    @Schema(description = "점주 ID", example = "1")
    Long id,

    @Schema(description = "점주 이름", example = "홍길동")
    String name,

    @Schema(description = "사업자등록번호", example = "123-45-67890")
    String businessRegistrationNumber,

    @Schema(description = "계정 상태", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    String status
) {
    public static CeoListItemResponse from(CeoListItemResult result) {
        return new CeoListItemResponse(
            result.id(),
            result.name(),
            result.businessRegistrationNumber(),
            result.status() != null ? result.status().name() : null
        );
    }
}
