package com.tastyhouse.ceoapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "ceo.seed")
public record CeoSeedProperties(
    @DefaultValue("ceo") String username,
    @DefaultValue(CeoSeedProperties.UNSET_PASSWORD) String password,
    @DefaultValue("점주") String name
) {
    public static final String UNSET_PASSWORD = "__UNSET__";

    public boolean isDefaultPassword() {
        return UNSET_PASSWORD.equals(password);
    }
}
