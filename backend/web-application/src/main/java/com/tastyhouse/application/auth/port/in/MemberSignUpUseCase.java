package com.tastyhouse.application.auth.port.in;

public interface MemberSignUpUseCase {

    Long signUp(AuthSignUpCommand command);
}
