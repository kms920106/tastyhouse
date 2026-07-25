package com.tastyhouse.core.domain.ceo.application.dto.command;

/**
 * 점주 계정 생성 커맨드.
 * encodedPassword는 application 호출 측(ceo-api)에서 인코딩하여 전달한다.
 */
public record CeoCreateCommand(
    String username,
    String encodedPassword,
    String name
) {

    public static CeoCreateCommand of(String username, String encodedPassword, String name) {
        return new CeoCreateCommand(username, encodedPassword, name);
    }
}
