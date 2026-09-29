package com.tastyhouse.application.admin.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.application.admin.port.in.AdminQueryUseCase;
import com.tastyhouse.application.admin.port.out.write.AdminPersistencePort;
import com.tastyhouse.application.shared.marker.AdminApp;

@Service
@AdminApp
@Transactional(readOnly = true)
public class AdminQueryService implements AdminQueryUseCase {

    private final AdminPersistencePort adminPersistencePort;

    public AdminQueryService(AdminPersistencePort adminPersistencePort) {
        this.adminPersistencePort = adminPersistencePort;
    }

    public Optional<Admin> findByUsername(String username) {
        return adminPersistencePort.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return adminPersistencePort.existsByUsername(username);
    }
}
