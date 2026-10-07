package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.out.MemberJwtResult;

public interface MemberRefreshUseCase {

    MemberJwtResult refresh(String refreshToken);
}
