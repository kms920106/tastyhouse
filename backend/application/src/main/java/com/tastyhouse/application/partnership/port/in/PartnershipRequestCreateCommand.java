package com.tastyhouse.application.partnership.port.in;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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
