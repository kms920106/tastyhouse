package com.tastyhouse.webapi.partnership.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "제휴 문의 응답")
public record PartnershipRequestResponse(
    @Schema(description = "제휴 문의 ID", example = "1")
    Long id,

    @Schema(description = "사업체명", example = "테이스티하우스")
    String businessName,

    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
    String address,

    @Schema(description = "상세 주소", example = "4층 401호")
    String addressDetail,

    @Schema(description = "담당자명", example = "홍길동")
    String contactName,

    @Schema(description = "담당자 연락처", example = "010-1234-5678")
    String contactPhone,

    @Schema(description = "상담 희망 일시", example = "2026-01-05T14:00:00")
    LocalDateTime consultationRequestedAt,

    @Schema(description = "문의 등록 일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt
) {
    public static PartnershipRequestResponse from(
        Long id,
        String businessName,
        String address,
        String addressDetail,
        String contactName,
        String contactPhone,
        LocalDateTime consultationRequestedAt,
        LocalDateTime createdAt
    ) {
        return new PartnershipRequestResponse(
            id,
            businessName,
            address,
            addressDetail,
            contactName,
            contactPhone,
            consultationRequestedAt,
            createdAt
        );
    }
}
