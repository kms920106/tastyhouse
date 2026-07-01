package com.tastyhouse.core.domain.admin.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.admin.application.dto.command.CreateAdminCommand;
import com.tastyhouse.core.domain.admin.domain.model.Admin;
import com.tastyhouse.core.domain.admin.domain.repository.AdminRepository;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminCommandService {

    private final AdminRepository adminRepository;

    /**
     * 신규 관리자 계정을 생성한다. password는 이미 인코딩된 값이어야 한다.
     * username 중복 시 예외를 던진다.
     */
    public Long createAdmin(CreateAdminCommand command) {
        if (adminRepository.existsByUsername(command.username())) {
            throw new BusinessException(ErrorCode.ADMIN_USERNAME_DUPLICATED);
        }

        Admin admin = Admin.create(
            command.username(),
            command.encodedPassword(),
            command.name(),
            command.role()
        );

        return adminRepository.save(admin).getId();
    }
}
