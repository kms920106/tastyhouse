package com.tastyhouse.webapi.partnership.application.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 제휴 신청 생성 command.
 *
 * <p>형식·길이 검증은 Request의 jakarta.validation이 담당하고(400 계약·한국어 메시지 유지),
 * 이 record는 필수값 누락 같은 구조적 가드만 둔다.
 */
public record PartnershipRequestCreateCommand(
    String businessName,
    String address,
    String addressDetail,
    String contactName,
    String contactPhone,
    LocalDateTime consultationRequestedAt
) {
    public PartnershipRequestCreateCommand {
        if (businessName == null || address == null || contactName == null
            || contactPhone == null || consultationRequestedAt == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
