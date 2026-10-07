package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.out.AdminJwtResult;

public interface AdminTokenRefreshUseCase {

    AdminJwtResult refresh(String refreshToken);
}
