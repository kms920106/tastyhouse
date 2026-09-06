package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface MemberManagementCommandUseCase {

    void suspend(MemberSuspendCommand command);

    void activate(MemberActivateCommand command);

    void withdraw(MemberManagementWithdrawCommand command);
}
