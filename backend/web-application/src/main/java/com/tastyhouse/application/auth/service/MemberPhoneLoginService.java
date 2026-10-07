package com.tastyhouse.application.auth.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.in.MemberPhoneLoginUseCase;
import com.tastyhouse.application.auth.port.out.PhoneLoginResult;

@Service
class MemberPhoneLoginService implements MemberPhoneLoginUseCase {

    private final PhoneLoginService phoneLoginService;

    public MemberPhoneLoginService(PhoneLoginService phoneLoginService) {
        this.phoneLoginService = phoneLoginService;
    }

    @Override
    public PhoneLoginResult phoneLogin(String smsVerifyToken) {
        return phoneLoginService.login(smsVerifyToken);
    }
}
