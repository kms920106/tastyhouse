package com.tastyhouse.application.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.admin.port.out.write.AdminStatePort;
import com.tastyhouse.application.admin.store.AdminRepository;
import com.tastyhouse.application.admin.store.AdminStore;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class AdminServiceConfig {
    @Bean
    public AdminRepository adminRepository(AdminStatePort adminStatePort) {
        return new AdminStore(adminStatePort);
    }
}
