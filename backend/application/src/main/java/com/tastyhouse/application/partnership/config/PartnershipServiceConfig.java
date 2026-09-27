package com.tastyhouse.application.partnership.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.partnership.port.out.write.PartnershipRequestStatePort;
import com.tastyhouse.application.partnership.store.PartnershipRepository;
import com.tastyhouse.application.partnership.store.PartnershipStore;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class PartnershipServiceConfig {
    @Bean
    public PartnershipRepository partnershipRepository(PartnershipRequestStatePort partnershipRequestStatePort) {
        return new PartnershipStore(partnershipRequestStatePort);
    }
}
