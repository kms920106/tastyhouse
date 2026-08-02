package com.tastyhouse.infrastructure.admin.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.admin.domain.model.Admin;
import com.tastyhouse.domain.admin.domain.repository.AdminRepository;

@Repository
public class AdminRepositoryImpl implements AdminRepository {

    private final AdminJpaRepository adminJpaRepository;

    public AdminRepositoryImpl(AdminJpaRepository adminJpaRepository) {
        this.adminJpaRepository = adminJpaRepository;
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
