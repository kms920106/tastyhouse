package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public interface MemberCommandUseCase {

    void updateProfile(MemberProfileUpdateCommand command);

    void updatePersonalInfo(MemberPersonalInfoUpdateCommand command);

    void updatePassword(MemberPasswordUpdateCommand command);

    void withdraw(MemberWithdrawCommand command);
}
