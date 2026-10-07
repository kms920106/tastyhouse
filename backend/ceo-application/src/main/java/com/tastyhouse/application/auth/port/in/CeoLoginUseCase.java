package com.tastyhouse.application.auth.port.in;

import com.tastyhouse.application.auth.port.out.CeoJwtResult;

public interface CeoLoginUseCase {

    CeoJwtResult login(CeoAuthLoginCommand command);
}
