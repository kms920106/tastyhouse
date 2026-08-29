package com.tastyhouse.adminapi.member.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 회원 강제 탈퇴 command. 경로 변수 {@code id}는 컨트롤러가 {@code toCommand(id)}로 주입한다.
 *
 * <p>{@code reasonDetail}은 관리자 메모라 선택값이므로 null을 허용한다.
 */
public record MemberWithdrawCommand(
    Long memberId,
    String reason,
    String reasonDetail
) {
    public MemberWithdrawCommand {
        if (memberId == null || reason == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }
}
