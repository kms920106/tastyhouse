package com.tastyhouse.adminapi.ceo.response;

import io.swagger.v3.oas.annotations.media.Schema;

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
    public static CeoListItemResponse of(
        Long id,
        String name,
        String businessRegistrationNumber,
        String status
    ) {
        return new CeoListItemResponse(
            id,
            name,
            businessRegistrationNumber,
            status
        );
    }
}
