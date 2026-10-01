package com.tastyhouse.application.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.search.port.out.KeywordCountPort;
import com.tastyhouse.application.search.port.out.write.PopularKeywordPersistencePort;
import com.tastyhouse.application.search.port.out.write.SearchKeywordLogPersistencePort;
import com.tastyhouse.application.search.service.PopularKeywordRefreshService;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class SearchServiceConfig {

    @Bean
    public PopularKeywordRefreshService popularKeywordRefreshService(
        SearchKeywordLogPersistencePort searchKeywordLogPersistencePort,
        KeywordCountPort keywordCountPort,
        PopularKeywordPersistencePort popularKeywordPersistencePort
    ) {
        return new PopularKeywordRefreshService(searchKeywordLogPersistencePort, keywordCountPort, popularKeywordPersistencePort);
    }
}
