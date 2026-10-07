package com.tastyhouse.application.auth.port.in;

public interface AdminLogoutUseCase {

    void logout(String bearerToken);
}
