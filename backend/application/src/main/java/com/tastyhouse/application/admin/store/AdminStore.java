package com.tastyhouse.application.admin.store;

import java.util.Optional;

import com.tastyhouse.application.admin.port.out.write.AdminStatePort;
import com.tastyhouse.domain.admin.model.Admin;

public class AdminStore implements AdminRepository {
    private final AdminStatePort adminStatePort;

    public AdminStore(AdminStatePort adminStatePort) {
        this.adminStatePort = adminStatePort;
    }

    @Override
    public Optional<Admin> findByUsername(String username) {
        return adminStatePort.findByUsername(username).map(AdminStateMapper::toDomain);
    }

    @Override
    public boolean existsByUsername(String username) {
        return adminStatePort.existsByUsername(username);
    }

    @Override
    public Admin save(Admin admin) {
        return AdminStateMapper.toDomain(adminStatePort.save(AdminStateMapper.toState(admin)));
    }
}
