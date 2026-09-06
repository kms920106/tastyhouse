package com.tastyhouse.external.bbq;

import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.tastyhouse.external.crawling.bbq.BbqApiClient;
import com.tastyhouse.external.crawling.bbq.BbqProperties;
import com.tastyhouse.external.crawling.bbq.dto.BbqMenuCategoryResponse;

import static org.assertj.core.api.Assertions.assertThat;

@Disabled("실네트워크(bbq.co.kr) 호출 — 빌드 게이트에서 제외")
@SpringBootTest(classes = BbqApiClientTest.TestConfig.class)
class BbqApiClientTest {

    private static final Logger log = LoggerFactory.getLogger(BbqApiClientTest.class);

    @Autowired
    private BbqApiClient bbqApiClient;

    @Test
    void getMenuCategories() {

        List<BbqMenuCategoryResponse> categories = bbqApiClient.getMenuCategoriesSync();

        assertThat(categories).isNotNull();
        assertThat(categories).isNotEmpty();

        categories.forEach(category ->
                log.info("id={}, name={}, priority={}, isFullSize={}",
                        category.getId(),
                        category.getCategoryName(),
                        category.getPriority(),
                        category.getFullSize())
        );
    }

    @Configuration
    static class TestConfig {

        @Bean
        public WebClient.Builder webClientBuilder() {
            return WebClient.builder();
        }

        @Bean
        public BbqProperties bbqProperties() {
            return new BbqProperties("https://bbq.co.kr", 10);
        }

        @Bean
        public BbqApiClient bbqApiClient(WebClient.Builder webClientBuilder, BbqProperties bbqProperties) {
            return new BbqApiClient(webClientBuilder, bbqProperties);
        }
    }
}
