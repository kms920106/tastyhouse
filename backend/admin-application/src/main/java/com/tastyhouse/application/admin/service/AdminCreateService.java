package com.tastyhouse.application.admin.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.domain.admin.model.AdminRole;
import com.tastyhouse.application.admin.port.in.AdminCreateCommand;
import com.tastyhouse.application.admin.port.in.AdminCreateUseCase;
import com.tastyhouse.application.admin.port.out.write.AdminLoadPort;
import com.tastyhouse.application.admin.port.out.write.AdminSavePort;
import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;

@Service
@Transactional
class AdminCreateService implements AdminCreateUseCase {

    private final AdminLoadPort adminLoadPort;
    private final AdminSavePort adminSavePort;
    private final PasswordEncoder passwordEncoder;

    public AdminCreateService(AdminLoadPort adminLoadPort, AdminSavePort adminSavePort, PasswordEncoder passwordEncoder) {
        this.adminLoadPort = adminLoadPort;
        this.adminSavePort = adminSavePort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Long createAdmin(AdminCreateCommand command) {
        String username = command.username();
        if (adminLoadPort.existsByUsername(username)) {
            throw new ApplicationException(AdminErrorCode.ADMIN_USERNAME_DUPLICATED);
        }

        Admin admin = Admin.create(
            username,
            passwordEncoder.encode(command.password()),
            command.name(),
            AdminRole.from(command.role())
        );

        return adminSavePort.save(admin).getAdminId().value();
    }
}
