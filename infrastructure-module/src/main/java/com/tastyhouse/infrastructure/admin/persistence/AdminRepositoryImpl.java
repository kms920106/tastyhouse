package com.tastyhouse.infrastructure.admin.persistence;

import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.admin.domain.model.Admin;
import com.tastyhouse.core.domain.admin.domain.repository.AdminRepository;
import com.tastyhouse.core.domain.admin.domain.vo.AdminId;

@Repository
@RequiredArgsConstructor
public class AdminRepositoryImpl implements AdminRepository {

    private final AdminJpaRepository adminJpaRepository;

    @Override
    public Optional<Admin> findById(AdminId adminId) {
        return adminJpaRepository.findById(adminId.value()).map(AdminMapper::toDomain);
    }

    @Override
    public Optional<Admin> findByUsername(String username) {
        return adminJpaRepository.findByUsername(username).map(AdminMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return adminJpaRepository.existsByUsername(username);
    }

    @Override
    public Admin save(Admin admin) {
        AdminJpaEntity saved = adminJpaRepository.save(AdminMapper.toEntity(admin));
        return AdminMapper.toDomain(saved);
    }
}
