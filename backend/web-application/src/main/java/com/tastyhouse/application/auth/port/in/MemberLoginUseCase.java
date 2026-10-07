package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.out.MemberJwtResult;

public interface MemberLoginUseCase {

    MemberJwtResult login(String username, String password, boolean rememberMe);
}
