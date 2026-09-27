package com.tastyhouse.application.bug.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.bug.port.out.write.BugReportImageStatePort;
import com.tastyhouse.application.bug.port.out.write.BugReportStatePort;
import com.tastyhouse.application.bug.service.BugReportRegistrationService;
import com.tastyhouse.application.bug.store.BugReportImageRepository;
import com.tastyhouse.application.bug.store.BugReportImageStore;
import com.tastyhouse.application.bug.store.BugReportRepository;
import com.tastyhouse.application.bug.store.BugReportStore;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class BugServiceConfig {
    @Bean
    public BugReportRepository bugReportRepository(BugReportStatePort bugReportStatePort) {
        return new BugReportStore(bugReportStatePort);
    }

    @Bean
    public BugReportImageRepository bugReportImageRepository(BugReportImageStatePort bugReportImageStatePort) {
        return new BugReportImageStore(bugReportImageStatePort);
    }

    @Bean
    public BugReportRegistrationService bugReportRegistrationService(
        BugReportRepository bugReportRepository,
        BugReportImageRepository bugReportImageRepository
    ) {
        return new BugReportRegistrationService(bugReportRepository, bugReportImageRepository);
    }
}
