package com.tastyhouse.application.member.port.in;

public interface MemberVerifiedPersonalInfoUpdateUseCase {

    void updatePersonalInfo(MemberPersonalInfoUpdateCommand command, String verifyToken, String smsVerifyToken);
}
