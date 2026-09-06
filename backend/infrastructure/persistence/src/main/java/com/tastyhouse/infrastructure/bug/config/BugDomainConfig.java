package com.tastyhouse.infrastructure.bug.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.bug.repository.BugReportImageRepository;
import com.tastyhouse.domain.bug.repository.BugReportRepository;
import com.tastyhouse.domain.bug.service.BugReportRegistrationService;

@Configuration(proxyBeanMethods = false)
public class BugDomainConfig {
    @Bean
    public BugReportRegistrationService bugReportRegistrationService(
        BugReportRepository bugReportRepository,
        BugReportImageRepository bugReportImageRepository
    ) {
        return new BugReportRegistrationService(bugReportRepository, bugReportImageRepository);
    }
}
