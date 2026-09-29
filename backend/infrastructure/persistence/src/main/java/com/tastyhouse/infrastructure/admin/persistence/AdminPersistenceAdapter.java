package com.tastyhouse.infrastructure.admin.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.application.admin.port.out.write.AdminPersistencePort;

@Repository
public class AdminPersistenceAdapter implements AdminPersistencePort {
    private final AdminJpaRepository adminJpaRepository;

    public AdminPersistenceAdapter(AdminJpaRepository adminJpaRepository) {
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
