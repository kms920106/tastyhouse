package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

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
            throw new ApplicationException(ApplicationErrorCode.INVALID_INPUT);
        }
    }
}
