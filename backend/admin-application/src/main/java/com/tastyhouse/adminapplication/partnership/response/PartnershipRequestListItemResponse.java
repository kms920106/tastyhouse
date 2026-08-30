package com.tastyhouse.adminapplication.partnership.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "제휴 신청 목록 항목 응답")
public record PartnershipRequestListItemResponse(
    @Schema(description = "제휴 신청 ID", example = "1")
    Long id,

    @Schema(description = "상호명", example = "맛집식당")
    String businessName,

    @Schema(description = "담당자명", example = "홍길동")
    String contactName,

    @Schema(description = "담당자 연락처", example = "010-1234-5678")
    String contactPhone,

    @Schema(description = "처리 상태", example = "PENDING")
    String status,

    @Schema(description = "상담 희망 일시", example = "2026-03-01T14:00:00")
    LocalDateTime consultationRequestedAt,

    @Schema(description = "접수 일시", example = "2026-02-20T10:15:00")
    LocalDateTime createdAt
) {
    public static PartnershipRequestListItemResponse from(Long id, String businessName, String contactName, String contactPhone,
                                                            String status, LocalDateTime consultationRequestedAt,
                                                            LocalDateTime createdAt) {
        return new PartnershipRequestListItemResponse(
            id,
            businessName,
            contactName,
            contactPhone,
            status,
            consultationRequestedAt,
            createdAt
        );
    }
}
