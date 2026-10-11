package com.tastyhouse.application.phoneverification.port.out.write;

import com.tastyhouse.domain.phoneverification.model.SmsVerification;

public interface SmsVerificationSavePort {

    SmsVerification save(SmsVerification smsVerification);

    void expireAllPendingByPhoneNumber(String phoneNumber);
}
