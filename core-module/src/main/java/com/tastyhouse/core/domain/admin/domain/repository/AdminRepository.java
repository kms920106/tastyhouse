package com.tastyhouse.core.domain.admin.domain.repository;

import com.tastyhouse.core.domain.admin.domain.model.Admin;
import com.tastyhouse.core.domain.admin.domain.vo.AdminId;

import java.util.Optional;

public interface AdminRepository {

    Optional<Admin> findById(AdminId adminId);

    Optional<Admin> findByUsername(String username);

    boolean existsByUsername(String username);

    Admin save(Admin admin);
}
