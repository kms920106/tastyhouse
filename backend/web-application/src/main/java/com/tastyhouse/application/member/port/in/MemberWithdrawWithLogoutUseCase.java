package com.tastyhouse.application.member.port.in;

public interface MemberWithdrawWithLogoutUseCase {

    void withdrawMember(MemberWithdrawCommand command, String bearerToken);
}
