package com.tastyhouse.infrastructure.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.search.port.KeywordCountPort;
import com.tastyhouse.domain.search.repository.PopularKeywordRepository;
import com.tastyhouse.domain.search.repository.SearchKeywordLogRepository;
import com.tastyhouse.domain.search.service.PopularKeywordRefreshService;

@Configuration(proxyBeanMethods = false)
public class SearchDomainConfig {
    @Bean
    public PopularKeywordRefreshService popularKeywordRefreshService(
        SearchKeywordLogRepository searchKeywordLogRepository,
        KeywordCountPort keywordCountPort,
        PopularKeywordRepository popularKeywordRepository
    ) {
        return new PopularKeywordRefreshService(searchKeywordLogRepository, keywordCountPort, popularKeywordRepository);
    }
}
