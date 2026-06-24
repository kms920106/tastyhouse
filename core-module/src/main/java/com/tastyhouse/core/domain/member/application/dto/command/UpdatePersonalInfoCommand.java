package com.tastyhouse.core.domain.member.application.dto.command;

import com.tastyhouse.core.domain.member.domain.model.Gender;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record UpdatePersonalInfoCommand(
    MemberId memberId,
    String fullName,
    String phoneNumber,
    Integer birthDate,
    Gender gender,
    boolean pushNotificationEnabled,
    boolean marketingInfoEnabled,
    boolean eventInfoEnabled
) {
}
