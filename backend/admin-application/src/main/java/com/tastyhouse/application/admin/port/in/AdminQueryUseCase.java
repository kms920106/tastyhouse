package com.tastyhouse.application.admin.port.in;

public interface AdminQueryUseCase {

    boolean existsByUsername(String username);
}
