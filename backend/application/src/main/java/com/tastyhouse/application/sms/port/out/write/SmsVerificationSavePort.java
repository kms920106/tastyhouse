package com.tastyhouse.application.sms.port.out.write;

import com.tastyhouse.domain.sms.model.SmsVerification;

public interface SmsVerificationSavePort {

    SmsVerification save(SmsVerification smsVerification);

    void expireAllPendingByPhoneNumber(String phoneNumber);
}
