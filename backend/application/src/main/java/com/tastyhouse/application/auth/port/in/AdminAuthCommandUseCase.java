package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.out.AdminJwtResult;
import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface AdminAuthCommandUseCase {

    AdminJwtResult login(AdminAuthLoginCommand command);

    AdminJwtResult refresh(String refreshToken);

    void logout(String bearerToken);
}
