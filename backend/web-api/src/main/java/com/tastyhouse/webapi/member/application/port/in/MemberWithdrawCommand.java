package com.tastyhouse.webapi.member.application.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 회원 탈퇴 command.
 *
 * <p>{@code reason}은 경계 타입인 {@code String}으로 받고 {@code MemberWithdrawalReason.from} 승격은
 * 서비스가 담당한다. {@code reasonDetail}은 선택값이라 null을 허용한다.
 *
 * <p>액세스 토큰 무효화는 이 command에 담지 않는다 — 탈퇴 커밋 이후에 일어나야 하는 별개 관심사라
 * 파사드({@code MemberService#withdrawMember})가 순서를 소유한다.
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
