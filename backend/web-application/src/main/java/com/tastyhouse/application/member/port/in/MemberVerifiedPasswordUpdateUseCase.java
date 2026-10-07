package com.tastyhouse.application.member.port.in;

public interface MemberVerifiedPasswordUpdateUseCase {

    void updatePassword(MemberPasswordUpdateCommand command, String verifyToken);
}
