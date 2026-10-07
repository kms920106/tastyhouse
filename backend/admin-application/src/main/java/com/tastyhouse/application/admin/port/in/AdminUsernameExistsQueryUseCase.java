package com.tastyhouse.application.admin.port.in;

public interface AdminUsernameExistsQueryUseCase {

    boolean existsByUsername(String username);
}
