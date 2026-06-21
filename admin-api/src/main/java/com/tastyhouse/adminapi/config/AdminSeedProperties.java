package com.tastyhouse.adminapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 최초 SUPER_ADMIN 시드 자격증명 (application.yml: admin.seed.*)
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "admin.seed")
public class AdminSeedProperties {

    /** ADMIN_SEED_PASSWORD 미설정 시의 센티넬 값. 이 값이면 시드를 거부(fail-fast)한다. */
    public static final String UNSET_PASSWORD = "__UNSET__";

    private String username = "admin";
    private String password = UNSET_PASSWORD;
    private String name = "최고관리자";

    /** 비밀번호가 외부에서 주입되지 않았는지(=기본 센티넬인지) 여부 */
    public boolean isDefaultPassword() {
        return UNSET_PASSWORD.equals(password);
    }
}
