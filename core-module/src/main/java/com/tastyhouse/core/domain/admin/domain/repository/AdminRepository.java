package com.tastyhouse.core.domain.admin.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.admin.domain.model.Admin;
import com.tastyhouse.core.domain.admin.domain.vo.AdminId;

public interface AdminRepository {

    Optional<Admin> findById(AdminId adminId);

    Optional<Admin> findByUsername(String username);

    boolean existsByUsername(String username);

    Admin save(Admin admin);
}
