package com.tastyhouse.webapplication.member.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 회원 비밀번호 변경 command.
 *
 * <p><b>{@code newPassword}·{@code newPasswordConfirm} 두 {@code String}이 연달아 있다</b> — 뒤바뀌어도
 * 확인값 일치 검사는 대칭이라 통과해버리므로 결함이 드러나지 않는다. {@code toCommand}는 이름 기반
 * 접근자로 각 값을 짚어 넘긴다.
 */
public record MemberPasswordUpdateCommand(
    Long memberId,
    String newPassword,
    String newPasswordConfirm
) {
    public MemberPasswordUpdateCommand {
        if (memberId == null || newPassword == null || newPasswordConfirm == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
