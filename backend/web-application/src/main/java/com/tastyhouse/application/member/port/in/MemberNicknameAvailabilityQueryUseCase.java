package com.tastyhouse.application.member.port.in;

public interface MemberNicknameAvailabilityQueryUseCase {

    boolean checkNicknameAvailability(String nickname);
}
