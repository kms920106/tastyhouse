package com.tastyhouse.application.member.port.in;

public interface MemberCommandUseCase {

    void updateProfile(MemberProfileUpdateCommand command);

    void updatePersonalInfo(MemberPersonalInfoUpdateCommand command);

    void updatePassword(MemberPasswordUpdateCommand command);

    void withdraw(MemberWithdrawCommand command);
}
