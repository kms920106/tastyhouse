package com.tastyhouse.application.member.port.out;

import java.time.LocalDateTime;

public record MemberManagementDetailResult(
    Long id,
    String username,
    String nickname,
    String fullName,
    String phoneNumber,
    String gender,
    Integer birthDate,
    String memberGrade,
    String memberStatus,
    String statusMessage,
    boolean pushNotificationEnabled,
    boolean marketingInfoEnabled,
    boolean eventInfoEnabled,
    LocalDateTime createdAt
) {
}
