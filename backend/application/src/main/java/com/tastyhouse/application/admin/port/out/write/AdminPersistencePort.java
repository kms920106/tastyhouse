package com.tastyhouse.application.admin.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.admin.model.Admin;

public interface AdminPersistencePort {

    Optional<Admin> findByUsername(String username);

    boolean existsByUsername(String username);

    Admin save(Admin admin);
}
