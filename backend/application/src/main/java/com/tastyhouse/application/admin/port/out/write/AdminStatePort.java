package com.tastyhouse.application.admin.port.out.write;

import java.util.Optional;

public interface AdminStatePort {
    Optional<AdminState> findByUsername(String username);

    boolean existsByUsername(String username);

    AdminState save(AdminState state);
}
