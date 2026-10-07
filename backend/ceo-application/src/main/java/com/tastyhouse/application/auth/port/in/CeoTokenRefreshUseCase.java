package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.out.CeoJwtResult;

public interface CeoTokenRefreshUseCase {

    CeoJwtResult refresh(String refreshToken);
}
