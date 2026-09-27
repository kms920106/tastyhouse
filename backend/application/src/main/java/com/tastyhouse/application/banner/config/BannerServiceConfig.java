package com.tastyhouse.application.banner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.banner.port.out.write.BannerStatePort;
import com.tastyhouse.application.banner.store.BannerRepository;
import com.tastyhouse.application.banner.store.BannerStore;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class BannerServiceConfig {
    @Bean
    public BannerRepository bannerRepository(BannerStatePort bannerStatePort) {
        return new BannerStore(bannerStatePort);
    }
}
