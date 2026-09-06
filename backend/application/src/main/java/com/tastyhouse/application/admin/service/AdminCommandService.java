package com.tastyhouse.application.admin.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.admin.port.in.AdminCommandUseCase;
import com.tastyhouse.application.admin.port.in.AdminCreateCommand;
import com.tastyhouse.domain.admin.model.Admin;
import com.tastyhouse.domain.admin.model.AdminRole;
import com.tastyhouse.domain.admin.repository.AdminRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

@Service
@AdminApp
@Transactional
public class AdminCommandService implements AdminCommandUseCase {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminCommandService(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Long createAdmin(AdminCreateCommand command) {
        String username = command.username();
        if (adminRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.ADMIN_USERNAME_DUPLICATED);
        }

        Admin admin = Admin.create(
            username,
            passwordEncoder.encode(command.password()),
            command.name(),
            AdminRole.from(command.role())
        );

        return adminRepository.save(admin).getAdminId().value();
    }
}
