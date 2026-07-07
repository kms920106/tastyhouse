package com.tastyhouse.adminapi.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.admin.application.AdminCommandService;
import com.tastyhouse.core.domain.admin.application.dto.command.AdminCreateCommand;
import com.tastyhouse.adminapi.admin.request.AdminCreateRequest;

/**
 * 관리자 계정 생성 서비스.
 * 비밀번호 인코딩(BCrypt)을 담당한 뒤 core-module의 AdminCommandService로 저장을 위임한다.
 * (core-module은 Spring Security 의존성이 없으므로 인코딩은 admin-api에서 수행)
 */
@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final AdminCommandService adminCommandService;
    private final PasswordEncoder passwordEncoder;

    public Long create(AdminCreateRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        return adminCommandService.createAdmin(AdminCreateCommand.of(
            request.username(),
            encodedPassword,
            request.name(),
            request.role()
        )).value();
    }
}
