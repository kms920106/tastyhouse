package com.tastyhouse.application.member.port.out;

public record MemberPersonalInfoResult(
    String username,
    String fullName,
    String phoneNumber,
    Integer birthDate,
    String gender,
    boolean pushNotificationEnabled,
    boolean marketingInfoEnabled,
    boolean eventInfoEnabled
) {
}
