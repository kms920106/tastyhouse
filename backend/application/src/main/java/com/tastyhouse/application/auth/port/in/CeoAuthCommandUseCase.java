package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.auth.port.out.CeoJwtResult;

@CeoApp
public interface CeoAuthCommandUseCase {

    CeoJwtResult login(CeoAuthLoginCommand command);

    CeoJwtResult refresh(String refreshToken);

    void logout(String bearerToken);
}
