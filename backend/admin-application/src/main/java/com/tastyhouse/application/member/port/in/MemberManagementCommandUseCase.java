package com.tastyhouse.application.member.port.in;

public interface MemberManagementCommandUseCase {

    void suspend(MemberSuspendCommand command);

    void activate(MemberActivateCommand command);

    void withdraw(MemberManagementWithdrawCommand command);
}
