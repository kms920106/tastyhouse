package com.tastyhouse.application.sms.port.out.write;

import java.util.Optional;

public interface SmsVerificationStatePort {
    SmsVerificationState save(SmsVerificationState state);

    Optional<SmsVerificationState> findLatestPendingByPhoneNumber(String phoneNumber, String status);

    void expireAllPendingByPhoneNumber(String phoneNumber);
}
