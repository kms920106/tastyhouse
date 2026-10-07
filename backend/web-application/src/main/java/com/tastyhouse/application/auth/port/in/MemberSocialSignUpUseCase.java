package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.in.AuthSocialSignUpCommand;
import com.tastyhouse.application.auth.port.out.MemberJwtResult;

public interface MemberSocialSignUpUseCase {

    MemberJwtResult socialSignUp(AuthSocialSignUpCommand command);
}
