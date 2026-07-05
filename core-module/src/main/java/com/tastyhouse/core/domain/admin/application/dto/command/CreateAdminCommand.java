package com.tastyhouse.core.domain.admin.application.dto.command;

import com.tastyhouse.core.domain.admin.domain.model.AdminRole;

/**
 * 관리자 계정 생성 커맨드.
 * encodedPassword는 application 호출 측(admin-api)에서 인코딩하여 전달한다.
 */
public record CreateAdminCommand(
    String username,
    String encodedPassword,
    String name,
    AdminRole role
) {

    public static CreateAdminCommand of(String username, String encodedPassword, String name, AdminRole role) {
        return new CreateAdminCommand(username, encodedPassword, name, role);
    }
}
