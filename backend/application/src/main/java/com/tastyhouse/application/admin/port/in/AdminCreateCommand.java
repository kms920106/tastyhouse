package com.tastyhouse.application.admin.port.in;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 관리자 계정 생성 command.
 *
 * <p>{@code password}는 아직 인코딩되지 않은 원문이며, BCrypt 인코딩은 서비스 계층이 수행한다.
 * {@code username}·{@code password}·{@code name}·{@code role}이 모두 같은 {@code String}이라
 * 순서가 뒤바뀌어도 컴파일된다. 조립은 반드시 이름 있는 접근자로 한다.
 */
public record AdminCreateCommand(
    String username,
    String password,
    String name,
    String role
) {
    public AdminCreateCommand {
        if (username == null || password == null || name == null || role == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
    }

    public static AdminCreateCommand of(String username, String password, String name, String role) {
        return new AdminCreateCommand(username, password, name, role);
    }
}
