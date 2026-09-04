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

/**
 * 관리자 계정 command 서비스.
 *
 * <p>domain write 포트({@link AdminRepository})만 주입해 생성을 수행한다. 조회는
 * {@code AdminQueryService}가 담당한다.
 *
 * <p>비밀번호 인코딩(BCrypt)은 core-module에 Spring Security 의존성이 없으므로 이 계층에서 수행한다.
 * {@code Admin}은 update 경로가 없는 insert 전용 애그리거트다.
 */
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

    /**
     * 신규 관리자 계정을 생성한다. username 중복 시 예외를 던진다.
     */
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
