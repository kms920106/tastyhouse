package com.tastyhouse.webapi.partnership.adapter.in.web.request;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.tastyhouse.webapi.partnership.application.port.in.PartnershipRequestCreateCommand;

@Schema(description = "광고 및 제휴 신청 요청")
public record PartnershipRequestCreateRequest(
    @NotBlank(message = "상호명은 필수입니다")
    @Size(max = 200, message = "상호명은 200자 이내로 입력해주세요")
    @Schema(description = "상호명", example = "맛집식당", requiredMode = Schema.RequiredMode.REQUIRED)
    String businessName,

    @NotBlank(message = "주소는 필수입니다")
    @Size(max = 500, message = "주소는 500자 이내로 입력해주세요")
    @Schema(description = "기본 주소", example = "서울특별시 강남구 테헤란로 123", requiredMode = Schema.RequiredMode.REQUIRED)
    String address,

    @Size(max = 500, message = "상세주소는 500자 이내로 입력해주세요")
    @Schema(description = "상세 주소", example = "2층 201호")
    String addressDetail,

    @NotBlank(message = "성명은 필수입니다")
    @Size(max = 100, message = "성명은 100자 이내로 입력해주세요")
    @Schema(description = "담당자 성명", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    String contactName,

    @NotBlank(message = "연락처는 필수입니다")
    @Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "올바른 휴대폰 번호 형식이 아닙니다")
    @Schema(description = "담당자 연락처", example = "010-1234-5678", requiredMode = Schema.RequiredMode.REQUIRED)
    String contactPhone,

    @NotNull(message = "상담신청시간은 필수입니다")
    @Schema(description = "상담 신청 희망 시간", example = "2026-03-01T14:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime consultationRequestedAt
) {

    public PartnershipRequestCreateCommand toCommand() {
        return new PartnershipRequestCreateCommand(businessName, address, addressDetail, contactName, contactPhone, consultationRequestedAt);
    }
}
