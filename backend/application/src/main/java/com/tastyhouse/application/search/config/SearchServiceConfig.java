package com.tastyhouse.application.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.search.port.out.KeywordCountPort;
import com.tastyhouse.application.search.port.out.write.PopularKeywordStatePort;
import com.tastyhouse.application.search.port.out.write.SearchKeywordLogStatePort;
import com.tastyhouse.application.search.service.PopularKeywordRefreshService;
import com.tastyhouse.application.search.store.PopularKeywordRepository;
import com.tastyhouse.application.search.store.PopularKeywordStore;
import com.tastyhouse.application.search.store.SearchKeywordLogRepository;
import com.tastyhouse.application.search.store.SearchKeywordLogStore;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class SearchServiceConfig {
    @Bean
    public PopularKeywordRepository popularKeywordRepository(PopularKeywordStatePort popularKeywordStatePort) {
        return new PopularKeywordStore(popularKeywordStatePort);
    }

    @Bean
    public SearchKeywordLogRepository searchKeywordLogRepository(SearchKeywordLogStatePort searchKeywordLogStatePort) {
        return new SearchKeywordLogStore(searchKeywordLogStatePort);
    }

    @Bean
    public PopularKeywordRefreshService popularKeywordRefreshService(
        SearchKeywordLogRepository searchKeywordLogRepository,
        KeywordCountPort keywordCountPort,
        PopularKeywordRepository popularKeywordRepository
    ) {
        return new PopularKeywordRefreshService(searchKeywordLogRepository, keywordCountPort, popularKeywordRepository);
    }
}
