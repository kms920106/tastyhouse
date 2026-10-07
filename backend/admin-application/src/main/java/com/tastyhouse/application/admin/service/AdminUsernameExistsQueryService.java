package com.tastyhouse.application.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.admin.port.in.AdminUsernameExistsQueryUseCase;
import com.tastyhouse.application.admin.port.out.write.AdminPersistencePort;

@Service
@Transactional(readOnly = true)
class AdminUsernameExistsQueryService implements AdminUsernameExistsQueryUseCase {

    private final AdminPersistencePort adminPersistencePort;

    public AdminUsernameExistsQueryService(AdminPersistencePort adminPersistencePort) {
        this.adminPersistencePort = adminPersistencePort;
    }

    @Override
    public boolean existsByUsername(String username) {
        return adminPersistencePort.existsByUsername(username);
    }
}
