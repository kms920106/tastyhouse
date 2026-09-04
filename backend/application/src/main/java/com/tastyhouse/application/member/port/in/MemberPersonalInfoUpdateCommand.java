package com.tastyhouse.application.member.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 회원 개인정보 변경 command.
 *
 * <p>{@code gender}는 경계 타입인 {@code String}으로 받고 {@code MemberGender.from} 승격은 서비스가
 * 담당한다.
 *
 * <p><b>{@code fullName}·{@code phoneNumber} 두 {@code String}과 알림 동의 {@code Boolean} 3개가 각각
 * 연달아 있다</b> — 위치 기반으로 옮기면 컴파일은 통과하고 값만 조용히 뒤바뀌므로, {@code toCommand}는
 * 이름 기반 접근자로 각 값을 짚어 넘긴다.
 *
 * <p>알림 동의 3종은 하위호환을 위해 null을 허용한다({@code Boolean.TRUE.equals}로 정규화).
 */
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
