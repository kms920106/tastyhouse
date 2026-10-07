package com.tastyhouse.application.ceo.port.in;

public interface CeoOwnerUsernameExistsQueryUseCase {

    boolean existsByUsername(String username);
}
