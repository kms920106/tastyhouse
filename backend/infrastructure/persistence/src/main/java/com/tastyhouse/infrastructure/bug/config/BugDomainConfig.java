package com.tastyhouse.infrastructure.bug.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.bug.repository.BugReportImageRepository;
import com.tastyhouse.domain.bug.repository.BugReportRepository;
import com.tastyhouse.domain.bug.service.BugReportRegistrationService;

/**
 * bug 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class BugDomainConfig {

    /**
     * 버그 제보 등록 — 제보 애그리거트와 첨부 이미지 애그리거트를 한 트랜잭션에서 함께 저장하는 오케스트레이션.
     */
    @Bean
    public BugReportRegistrationService bugReportRegistrationService(
        BugReportRepository bugReportRepository,
        BugReportImageRepository bugReportImageRepository
    ) {
        return new BugReportRegistrationService(bugReportRepository, bugReportImageRepository);
    }
}
