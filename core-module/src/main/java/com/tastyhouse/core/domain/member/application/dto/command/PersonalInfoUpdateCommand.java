package com.tastyhouse.core.domain.member.application.dto.command;

import com.tastyhouse.core.domain.member.domain.model.MemberGender;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record PersonalInfoUpdateCommand(
    MemberId memberId,
    String fullName,
    String phoneNumber,
    Integer birthDate,
    MemberGender gender,
    boolean pushNotificationEnabled,
    boolean marketingInfoEnabled,
    boolean eventInfoEnabled
) {

    public static PersonalInfoUpdateCommand of(
        MemberId memberId,
        String fullName,
        String phoneNumber,
        Integer birthDate,
        MemberGender gender,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled
    ) {
        return new PersonalInfoUpdateCommand(
            memberId, fullName, phoneNumber, birthDate, gender,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled
        );
    }
}
