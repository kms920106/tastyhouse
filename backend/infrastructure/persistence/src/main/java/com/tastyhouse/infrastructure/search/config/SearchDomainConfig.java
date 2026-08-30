package com.tastyhouse.infrastructure.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.search.port.KeywordCountPort;
import com.tastyhouse.domain.search.repository.PopularKeywordRepository;
import com.tastyhouse.domain.search.repository.SearchKeywordLogRepository;
import com.tastyhouse.domain.search.service.PopularKeywordRefreshService;

/**
 * search 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class SearchDomainConfig {

    /**
     * 인기 검색어 갱신 규칙 — 기존 목록 전체를 읽어 신규 여부를 판정하고 통째로 교체하는 크로스 인스턴스 오케스트레이션.
     */
    @Bean
    public PopularKeywordRefreshService popularKeywordRefreshService(
        SearchKeywordLogRepository searchKeywordLogRepository,
        KeywordCountPort keywordCountPort,
        PopularKeywordRepository popularKeywordRepository
    ) {
        return new PopularKeywordRefreshService(searchKeywordLogRepository, keywordCountPort, popularKeywordRepository);
    }
}
