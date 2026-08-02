package com.tastyhouse.ceoapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * 최초 점주 계정 시드 자격증명 (application.yml: ceo.seed.*)
 */
@ConfigurationProperties(prefix = "ceo.seed")
public record CeoSeedProperties(
    @DefaultValue("ceo") String username,
    @DefaultValue(CeoSeedProperties.UNSET_PASSWORD) String password,
    @DefaultValue("점주") String name
) {

    /** CEO_SEED_PASSWORD 미설정 시의 센티넬 값. 이 값이면 시드를 거부(fail-fast)한다. */
    public static final String UNSET_PASSWORD = "__UNSET__";

    /** 비밀번호가 외부에서 주입되지 않았는지(=기본 센티넬인지) 여부 */
    public boolean isDefaultPassword() {
        return UNSET_PASSWORD.equals(password);
    }
}
