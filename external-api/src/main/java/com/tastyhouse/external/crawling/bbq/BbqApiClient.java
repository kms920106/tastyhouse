package com.tastyhouse.external.crawling.bbq;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import com.tastyhouse.external.crawling.bbq.dto.BbqMenuCategoryResponse;
import com.tastyhouse.external.crawling.bbq.dto.BbqMenuResponse;
import com.tastyhouse.external.crawling.bbq.dto.BbqMenuSubOptionResponse;

/**
 * BBQ API 클라이언트
 */
@Component
public class BbqApiClient {

    private static final Logger log = LoggerFactory.getLogger(BbqApiClient.class);

    private final WebClient.Builder webClientBuilder;
    private final BbqProperties bbqProperties;

    public BbqApiClient(WebClient.Builder webClientBuilder, BbqProperties bbqProperties) {
        this.webClientBuilder = webClientBuilder;
        this.bbqProperties = bbqProperties;
    }

    private WebClient getWebClient() {
        return webClientBuilder.build();
    }

    /**
     * BBQ 메뉴 카테고리 목록 조회
     */
    public Mono<List<BbqMenuCategoryResponse>> getMenuCategories() {
        String url = bbqProperties.baseUrl() + "/api/delivery/menu/category";

        return handleApiError(
                getWebClient().get()
                        .uri(url)
                        .retrieve()
                        .bodyToFlux(BbqMenuCategoryResponse.class)
                        .collectList()
                        .timeout(Duration.ofSeconds(bbqProperties.timeoutSeconds()))
                        .doOnSuccess(categories ->
                                log.info("BBQ 메뉴 카테고리 조회 성공: {}개", categories.size())),
                "BBQ 메뉴 카테고리 조회"
        );
    }

    /**
     * BBQ 메뉴 카테고리 목록 조회 (동기 방식)
     */
    public List<BbqMenuCategoryResponse> getMenuCategoriesSync() {
        return getMenuCategories()
                .block(Duration.ofSeconds(bbqProperties.timeoutSeconds()));
    }

    /**
     * BBQ 카테고리별 메뉴 목록 조회
     */
    public Mono<List<BbqMenuResponse>> getMenusByCategoryId(Long categoryId) {
        String url = bbqProperties.baseUrl() + "/api/delivery/menu/" + categoryId;

        return handleApiError(
                getWebClient().get()
                        .uri(url)
                        .retrieve()
                        .bodyToFlux(BbqMenuResponse.class)
                        .collectList()
                        .timeout(Duration.ofSeconds(bbqProperties.timeoutSeconds()))
                        .doOnSuccess(menus ->
                                log.info("BBQ 카테고리별 메뉴 조회 성공: categoryId={}, 메뉴 수={}", categoryId, menus.size())),
                "BBQ 카테고리별 메뉴 조회"
        );
    }

    /**
     * BBQ 카테고리별 메뉴 목록 조회 (동기 방식)
     */
    public List<BbqMenuResponse> getMenusByCategoryIdSync(Long categoryId) {
        return getMenusByCategoryId(categoryId)
                .block(Duration.ofSeconds(bbqProperties.timeoutSeconds()));
    }

    /**
     * BBQ 메뉴 상세 조회
     */
    public Mono<BbqMenuResponse> getMenuDetail(Long menuId) {
        String url = bbqProperties.baseUrl() + "/api/delivery/menu/detail/" + menuId;

        return handleApiError(
                getWebClient().get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(BbqMenuResponse.class)
                        .timeout(Duration.ofSeconds(bbqProperties.timeoutSeconds()))
                        .doOnSuccess(menu ->
                                log.info("BBQ 메뉴 상세 조회 성공: menuId={}, menuName={}", menuId, menu.getMenuName())),
                "BBQ 메뉴 상세 조회"
        );
    }

    /**
     * BBQ 메뉴 상세 조회 (동기 방식)
     */
    public BbqMenuResponse getMenuDetailSync(Long menuId) {
        return getMenuDetail(menuId)
                .block(Duration.ofSeconds(bbqProperties.timeoutSeconds()));
    }

    /**
     * BBQ 메뉴 서브 옵션 조회
     */
    public Mono<List<BbqMenuSubOptionResponse>> getMenuSubOptions(Long menuId) {
        String url = bbqProperties.baseUrl() + "/api/delivery/menu/sub-option/" + menuId;

        return handleApiError(
                getWebClient().get()
                        .uri(url)
                        .retrieve()
                        .bodyToFlux(BbqMenuSubOptionResponse.class)
                        .collectList()
                        .timeout(Duration.ofSeconds(bbqProperties.timeoutSeconds()))
                        .doOnSuccess(subOptions ->
                                log.info("BBQ 메뉴 서브 옵션 조회 성공: menuId={}, 옵션 수={}", menuId, subOptions.size())),
                "BBQ 메뉴 서브 옵션 조회"
        );
    }

    /**
     * BBQ 메뉴 서브 옵션 조회 (동기 방식)
     */
    public List<BbqMenuSubOptionResponse> getMenuSubOptionsSync(Long menuId) {
        return getMenuSubOptions(menuId)
                .block(Duration.ofSeconds(bbqProperties.timeoutSeconds()));
    }

    private <T> Mono<T> handleApiError(Mono<T> mono, String apiName) {
        return mono
                .doOnError(WebClientResponseException.class, ex ->
                        log.error("{} 실패: Status={}, Message={}", apiName, ex.getStatusCode(), ex.getMessage()))
                .doOnError(Throwable.class, ex -> {
                    if (!(ex instanceof WebClientResponseException)) {
                        log.error("{} 중 예외 발생", apiName, ex);
                    }
                });
    }
}
