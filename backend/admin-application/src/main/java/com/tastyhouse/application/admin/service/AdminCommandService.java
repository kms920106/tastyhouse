package com.tastyhouse.application.admin.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.domain.admin.model.AdminRole;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.application.admin.port.in.AdminCommandUseCase;
import com.tastyhouse.application.admin.port.in.AdminCreateCommand;
import com.tastyhouse.application.admin.port.out.write.AdminPersistencePort;

@Service
@Transactional
public class AdminCommandService implements AdminCommandUseCase {

    private final AdminPersistencePort adminPersistencePort;
    private final PasswordEncoder passwordEncoder;

    public AdminCommandService(AdminPersistencePort adminPersistencePort, PasswordEncoder passwordEncoder) {
        this.adminPersistencePort = adminPersistencePort;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Long createAdmin(AdminCreateCommand command) {
        String username = command.username();
        if (adminPersistencePort.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.ADMIN_USERNAME_DUPLICATED);
        }

        Admin admin = Admin.create(
            username,
            passwordEncoder.encode(command.password()),
            command.name(),
            AdminRole.from(command.role())
        );

        return adminPersistencePort.save(admin).getAdminId().value();
    }
}
