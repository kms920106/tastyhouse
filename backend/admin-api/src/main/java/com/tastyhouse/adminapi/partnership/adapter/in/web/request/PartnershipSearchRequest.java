package com.tastyhouse.adminapi.partnership.adapter.in.web.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "제휴 신청 검색 요청")
public record PartnershipSearchRequest(
    @Schema(description = "상호명 (부분 일치 검색)", example = "맛집")
    String businessName,

    @Schema(description = "담당자명 (부분 일치 검색)", example = "홍길동")
    String contactName,

    @Schema(description = "담당자 연락처 (부분 일치 검색)", example = "010-1234")
    String contactPhone,

    @Schema(description = "처리 상태", example = "PENDING", allowableValues = {"PENDING", "IN_PROGRESS", "COMPLETED"})
    String status,

    @Schema(description = "접수 조회 시작 일시", example = "2026-01-01T00:00:00")
    LocalDateTime startDate,

    @Schema(description = "접수 조회 종료 일시", example = "2026-01-31T23:59:59")
    LocalDateTime endDate
) {
}
