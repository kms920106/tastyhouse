package com.tastyhouse.application.member.port.in;

public interface MemberPhoneAvailabilityQueryUseCase {

    boolean checkPhoneAvailability(String phoneNumber);
}
