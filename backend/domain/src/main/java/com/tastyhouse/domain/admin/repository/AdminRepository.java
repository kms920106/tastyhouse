package com.tastyhouse.domain.admin.repository;

import java.util.Optional;

import com.tastyhouse.domain.admin.model.Admin;

public interface AdminRepository {
    Optional<Admin> findByUsername(String username);

    boolean existsByUsername(String username);

    Admin save(Admin admin);
}
