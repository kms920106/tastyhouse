package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.in.AuthSignUpCommand;

public interface MemberSignUpUseCase {

    Long signUp(AuthSignUpCommand command);
}
