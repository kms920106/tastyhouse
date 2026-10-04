package com.tastyhouse.application.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.admin.port.in.AdminQueryUseCase;
import com.tastyhouse.application.admin.port.out.write.AdminPersistencePort;

@Service
@Transactional(readOnly = true)
public class AdminQueryService implements AdminQueryUseCase {

    private final AdminPersistencePort adminPersistencePort;

    public AdminQueryService(AdminPersistencePort adminPersistencePort) {
        this.adminPersistencePort = adminPersistencePort;
    }

    @Override
    public boolean existsByUsername(String username) {
        return adminPersistencePort.existsByUsername(username);
    }
}
