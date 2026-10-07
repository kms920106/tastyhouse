package com.tastyhouse.application.auth.port.in;

public interface CeoLogoutUseCase {

    void logout(String bearerToken);
}
