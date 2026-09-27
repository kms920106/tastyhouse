package com.tastyhouse.infrastructure.admin.persistence;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.tastyhouse.application.admin.port.out.write.AdminState;
import com.tastyhouse.application.admin.port.out.write.AdminStatePort;

@Repository
public class AdminStatePortImpl implements AdminStatePort {
    private final AdminJpaRepository adminJpaRepository;

    public AdminStatePortImpl(AdminJpaRepository adminJpaRepository) {
        this.adminJpaRepository = adminJpaRepository;
    }

    @Override
    public Optional<AdminState> findByUsername(String username) {
        return adminJpaRepository.findByUsername(username).map(AdminMapper::toState);
    }

    @Override
    public boolean existsByUsername(String username) {
        return adminJpaRepository.existsByUsername(username);
    }

    @Override
    public AdminState save(AdminState state) {
        AdminJpaEntity saved = adminJpaRepository.save(AdminMapper.toEntity(state));
        return AdminMapper.toState(saved);
    }
}
