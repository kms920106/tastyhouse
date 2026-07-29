package com.tastyhouse.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.core.domain.faq.domain.repository.FaqCategoryRepository;
import com.tastyhouse.core.domain.faq.domain.service.FaqCategoryDeletionPolicy;

/**
 * 하강된 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>core-module → domain-module 전환 과정에서, 분류 (C) 불변식 오케스트레이션 / (D) 무상태 정책에
 * 해당하는 도메인 서비스는 {@code @Service}/{@code @Transactional} 없는 순수 POJO로 하강한다
 * (공통 지침의 패턴 1). Spring이 이들을 스캔할 수 없으므로, 각 도메인 작업에서 하강시킨 POJO를
 * 이 클래스의 {@code @Bean} 메서드로 등록한다.
 *
 * <p>초기에는 빈 클래스로 시작하며, 도메인 작업이 진행되며 {@code @Bean} 정의가 채워진다.
 */
@Configuration
public class DomainServiceConfig {

    /**
     * FAQ 카테고리 삭제 규칙 — 소속된 활성 FAQ 항목이 남아 있으면 삭제를 막는 크로스 애그리거트 규칙.
     */
    @Bean
    public FaqCategoryDeletionPolicy faqCategoryDeletionPolicy(FaqCategoryRepository faqCategoryRepository) {
        return new FaqCategoryDeletionPolicy(faqCategoryRepository);
    }
}
