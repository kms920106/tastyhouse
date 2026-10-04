package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.out.CeoJwtResult;

public interface CeoAuthCommandUseCase {

    CeoJwtResult login(CeoAuthLoginCommand command);

    CeoJwtResult refresh(String refreshToken);

    void logout(String bearerToken);
}
