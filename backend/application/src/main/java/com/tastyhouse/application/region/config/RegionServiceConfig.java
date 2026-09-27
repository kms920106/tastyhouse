package com.tastyhouse.application.region.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.region.port.out.write.AdminDongStatePort;
import com.tastyhouse.application.region.store.AdminDongRepository;
import com.tastyhouse.application.region.store.AdminDongStore;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class RegionServiceConfig {
    @Bean
    public AdminDongRepository adminDongRepository(AdminDongStatePort adminDongStatePort) {
        return new AdminDongStore(adminDongStatePort);
    }
}
