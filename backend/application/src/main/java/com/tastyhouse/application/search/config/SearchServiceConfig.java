package com.tastyhouse.application.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.search.port.out.KeywordCountPort;
import com.tastyhouse.application.search.port.out.write.PopularKeywordRepository;
import com.tastyhouse.application.search.port.out.write.SearchKeywordLogRepository;
import com.tastyhouse.application.search.service.PopularKeywordRefreshService;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class SearchServiceConfig {
    @Bean
    public PopularKeywordRefreshService popularKeywordRefreshService(
        SearchKeywordLogRepository searchKeywordLogRepository,
        KeywordCountPort keywordCountPort,
        PopularKeywordRepository popularKeywordRepository
    ) {
        return new PopularKeywordRefreshService(searchKeywordLogRepository, keywordCountPort, popularKeywordRepository);
    }
}
