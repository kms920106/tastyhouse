package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.out.PhoneLoginResult;

public interface MemberPhoneLoginUseCase {

    PhoneLoginResult phoneLogin(String smsVerifyToken);
}
