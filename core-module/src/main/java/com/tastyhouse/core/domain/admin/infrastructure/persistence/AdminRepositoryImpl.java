package com.tastyhouse.core.domain.admin.infrastructure.persistence;

import com.tastyhouse.core.domain.admin.domain.model.Admin;
import com.tastyhouse.core.domain.admin.domain.repository.AdminRepository;
import com.tastyhouse.core.domain.admin.domain.vo.AdminId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdminRepositoryImpl implements AdminRepository {

    private final AdminJpaRepository adminJpaRepository;

    @Override
    public Optional<Admin> findById(AdminId adminId) {
        return adminJpaRepository.findById(adminId.value());
    }

    @Override
    public Optional<Admin> findByUsername(String username) {
        return adminJpaRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return adminJpaRepository.existsByUsername(username);
    }

    @Override
    public Admin save(Admin admin) {
        return adminJpaRepository.save(admin);
    }
}
