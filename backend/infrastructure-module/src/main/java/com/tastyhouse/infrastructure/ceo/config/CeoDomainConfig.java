package com.tastyhouse.infrastructure.ceo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.ceo.repository.CeoLoginHistoryRepository;
import com.tastyhouse.domain.ceo.service.CeoLoginHistoryRecorder;

/**
 * ceo 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class CeoDomainConfig {

    /**
     * 점주 로그인 이력 기록 — 개인정보처리시스템 접속기록. ceo-api의
     * {@code CeoLoginHistoryCommandService}가 트랜잭션 경계를 열고 호출한다.
     */
    @Bean
    public CeoLoginHistoryRecorder ceoLoginHistoryRecorder(
        CeoLoginHistoryRepository ceoLoginHistoryRepository
    ) {
        return new CeoLoginHistoryRecorder(ceoLoginHistoryRepository);
    }
}
