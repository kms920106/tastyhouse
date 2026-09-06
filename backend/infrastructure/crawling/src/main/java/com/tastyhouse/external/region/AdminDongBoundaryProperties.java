package com.tastyhouse.external.region;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "region.admin-dong.boundary")
public record AdminDongBoundaryProperties(
    @DefaultValue("https://raw.githubusercontent.com/vuski/admdongkor/master/ver20260701/HangJeongDong_ver20260701.geojson")
    String sourceUrl,

    @DefaultValue("180") int timeoutSeconds,

    @DefaultValue("134217728") int maxBytes
) {
}
