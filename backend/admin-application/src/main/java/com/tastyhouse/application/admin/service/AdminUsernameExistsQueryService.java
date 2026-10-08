package com.tastyhouse.application.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.admin.port.in.AdminUsernameExistsQueryUseCase;
import com.tastyhouse.application.admin.port.out.AdminQueryPort;

@Service
@Transactional(readOnly = true)
class AdminUsernameExistsQueryService implements AdminUsernameExistsQueryUseCase {

    private final AdminQueryPort adminQueryPort;

    public AdminUsernameExistsQueryService(AdminQueryPort adminQueryPort) {
        this.adminQueryPort = adminQueryPort;
    }

    @Override
    public boolean existsByUsername(String username) {
        return adminQueryPort.existsByUsername(username);
    }
}
