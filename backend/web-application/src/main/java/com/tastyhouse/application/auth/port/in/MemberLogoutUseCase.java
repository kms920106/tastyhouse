package com.tastyhouse.application.auth.port.in;

public interface MemberLogoutUseCase {

    void logout(String bearerToken);
}
