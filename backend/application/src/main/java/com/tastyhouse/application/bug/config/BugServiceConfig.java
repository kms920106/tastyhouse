package com.tastyhouse.application.bug.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.bug.port.out.write.BugReportImageRepository;
import com.tastyhouse.application.bug.port.out.write.BugReportRepository;
import com.tastyhouse.application.bug.service.BugReportRegistrationService;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class BugServiceConfig {
    @Bean
    public BugReportRegistrationService bugReportRegistrationService(
        BugReportRepository bugReportRepository,
        BugReportImageRepository bugReportImageRepository
    ) {
        return new BugReportRegistrationService(bugReportRepository, bugReportImageRepository);
    }
}
