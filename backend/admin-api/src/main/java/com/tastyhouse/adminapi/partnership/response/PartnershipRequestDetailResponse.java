package com.tastyhouse.adminapi.partnership.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "제휴 신청 상세 응답")
public record PartnershipRequestDetailResponse(
    @Schema(description = "제휴 신청 ID", example = "1")
    Long id,

    @Schema(description = "상호명", example = "맛집식당")
    String businessName,

    @Schema(description = "주소", example = "서울특별시 강남구 테헤란로 123")
    String address,

    @Schema(description = "상세 주소", example = "2층 201호")
    String addressDetail,

    @Schema(description = "담당자명", example = "홍길동")
    String contactName,

    @Schema(description = "담당자 연락처", example = "010-1234-5678")
    String contactPhone,

    @Schema(description = "처리 상태", example = "PENDING")
    String status,

    @Schema(description = "상담 희망 일시", example = "2026-03-01T14:00:00")
    LocalDateTime consultationRequestedAt,

    @Schema(description = "접수 일시", example = "2026-02-20T10:15:00")
    LocalDateTime createdAt,

    @Schema(description = "수정 일시", example = "2026-02-21T09:00:00")
    LocalDateTime updatedAt
) {
    public static PartnershipRequestDetailResponse from(Long id, String businessName, String address, String addressDetail,
                                                          String contactName, String contactPhone, String status,
                                                          LocalDateTime consultationRequestedAt, LocalDateTime createdAt,
                                                          LocalDateTime updatedAt) {
        return new PartnershipRequestDetailResponse(
            id,
            businessName,
            address,
            addressDetail,
            contactName,
            contactPhone,
            status,
            consultationRequestedAt,
            createdAt,
            updatedAt
        );
    }
}
