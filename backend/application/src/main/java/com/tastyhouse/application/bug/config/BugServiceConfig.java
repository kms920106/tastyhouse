package com.tastyhouse.application.bug.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.bug.port.out.write.BugReportImagePersistencePort;
import com.tastyhouse.application.bug.port.out.write.BugReportPersistencePort;
import com.tastyhouse.application.bug.service.BugReportRegistrationService;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class BugServiceConfig {

    @Bean
    public BugReportRegistrationService bugReportRegistrationService(
        BugReportPersistencePort bugReportPersistencePort,
        BugReportImagePersistencePort bugReportImagePersistencePort
    ) {
        return new BugReportRegistrationService(bugReportPersistencePort, bugReportImagePersistencePort);
    }
}
