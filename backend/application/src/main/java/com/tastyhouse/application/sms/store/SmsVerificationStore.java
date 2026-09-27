package com.tastyhouse.application.sms.store;

import java.util.Optional;

import com.tastyhouse.application.sms.port.out.write.SmsVerificationStatePort;
import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;

public class SmsVerificationStore implements SmsVerificationRepository {
    private final SmsVerificationStatePort smsVerificationStatePort;

    public SmsVerificationStore(SmsVerificationStatePort smsVerificationStatePort) {
        this.smsVerificationStatePort = smsVerificationStatePort;
    }

    @Override
    public SmsVerification save(SmsVerification smsVerification) {
        return SmsVerificationStateMapper.toDomain(
            smsVerificationStatePort.save(SmsVerificationStateMapper.toState(smsVerification)));
    }

    @Override
    public Optional<SmsVerification> findLatestPendingByPhoneNumber(String phoneNumber, SmsVerificationStatus status) {
        return smsVerificationStatePort.findLatestPendingByPhoneNumber(phoneNumber, status == null ? null : status.name())
            .map(SmsVerificationStateMapper::toDomain);
    }

    @Override
    public void expireAllPendingByPhoneNumber(String phoneNumber) {
        smsVerificationStatePort.expireAllPendingByPhoneNumber(phoneNumber);
    }
}
