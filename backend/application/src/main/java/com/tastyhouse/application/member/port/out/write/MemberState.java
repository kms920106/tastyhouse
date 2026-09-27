package com.tastyhouse.application.member.port.out.write;

import java.time.LocalDateTime;

public record MemberState(
    Long id,
    String username,
    String password,
    String nickname,
    String fullName,
    Integer birthDate,
    String gender,
    String phoneNumber,
    String memberGrade,
    Long profileImageFileId,
    String statusMessage,
    boolean pushNotificationEnabled,
    boolean marketingInfoEnabled,
    boolean eventInfoEnabled,
    String memberStatus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
