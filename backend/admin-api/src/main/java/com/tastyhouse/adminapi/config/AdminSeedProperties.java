package com.tastyhouse.adminapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "admin.seed")
public record AdminSeedProperties(
    @DefaultValue("admin") String username,
    @DefaultValue(AdminSeedProperties.UNSET_PASSWORD) String password,
    @DefaultValue("최고관리자") String name
) {
    public static final String UNSET_PASSWORD = "__UNSET__";

    public boolean isDefaultPassword() {
        return UNSET_PASSWORD.equals(password);
    }
}
