package com.tastyhouse.application.member.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

public record MemberPersonalInfoUpdateCommand(
    Long memberId,
    String fullName,
    String phoneNumber,
    Integer birthDate,
    String gender,
    Boolean pushNotificationEnabled,
    Boolean marketingInfoEnabled,
    Boolean eventInfoEnabled
) {
    public MemberPersonalInfoUpdateCommand {
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
