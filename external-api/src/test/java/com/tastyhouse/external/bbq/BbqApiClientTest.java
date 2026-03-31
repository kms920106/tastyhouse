package com.tastyhouse.external.bbq;

import com.tastyhouse.external.bbq.dto.BbqMenuCategoryResponse;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BBQ API 실제 호출 테스트
 * 실제 네트워크 호출을 통해 응답 값을 검증합니다.
 */
@SpringBootTest(classes = BbqApiClientTest.TestConfig.class)
class BbqApiClientTest {

    private static final Logger log = LoggerFactory.getLogger(BbqApiClientTest.class);

    @Autowired
    private BbqApiClient bbqApiClient;

    @Test
    void getMenuCategories() {
        // given

        // when
        List<BbqMenuCategoryResponse> categories = bbqApiClient.getMenuCategoriesSync();

        // then
        assertThat(categories).isNotNull();
        assertThat(categories).isNotEmpty();

        categories.forEach(category ->
                log.info("id={}, name={}, priority={}, isFullSize={}",
                        category.getId(),
                        category.getCategoryName(),
                        category.getPriority(),
                        category.getIsFullSize())
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
            return new BbqProperties();
        }

        @Bean
        public BbqApiClient bbqApiClient(WebClient.Builder webClientBuilder, BbqProperties bbqProperties) {
            return new BbqApiClient(webClientBuilder, bbqProperties);
        }
    }
}
