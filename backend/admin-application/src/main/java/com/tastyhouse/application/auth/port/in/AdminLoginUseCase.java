package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.out.AdminJwtResult;

public interface AdminLoginUseCase {

    AdminJwtResult login(AdminAuthLoginCommand command);
}
