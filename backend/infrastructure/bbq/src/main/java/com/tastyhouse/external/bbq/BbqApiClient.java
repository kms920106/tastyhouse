package com.tastyhouse.external.bbq;

import java.util.List;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.tastyhouse.external.bbq.dto.BbqMenuCategoryResponse;
import com.tastyhouse.external.bbq.dto.BbqMenuResponse;
import com.tastyhouse.external.bbq.dto.BbqMenuSubOptionResponse;

@Component
public class BbqApiClient {

    private static final Logger log = LoggerFactory.getLogger(BbqApiClient.class);

    private static final ParameterizedTypeReference<List<BbqMenuCategoryResponse>> CATEGORY_LIST =
        new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<BbqMenuResponse>> MENU_LIST =
        new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<List<BbqMenuSubOptionResponse>> SUB_OPTION_LIST =
        new ParameterizedTypeReference<>() {};

    private final RestClient restClient;

    public BbqApiClient(RestClient.Builder restClientBuilder, BbqProperties bbqProperties) {
        this.restClient = restClientBuilder.baseUrl(bbqProperties.baseUrl()).build();
    }

    public List<BbqMenuCategoryResponse> getMenuCategories() {
        List<BbqMenuCategoryResponse> categories = call(
            () -> restClient.get()
                .uri("/api/delivery/menu/category")
                .retrieve()
                .body(CATEGORY_LIST),
            "BBQ 메뉴 카테고리 조회"
        );
        log.info("BBQ 메뉴 카테고리 조회 성공: {}개", sizeOf(categories));
        return categories;
    }

    public List<BbqMenuResponse> getMenusByCategoryId(Long categoryId) {
        List<BbqMenuResponse> menus = call(
            () -> restClient.get()
                .uri("/api/delivery/menu/{categoryId}", categoryId)
                .retrieve()
                .body(MENU_LIST),
            "BBQ 카테고리별 메뉴 조회"
        );
        log.info("BBQ 카테고리별 메뉴 조회 성공: categoryId={}, 메뉴 수={}", categoryId, sizeOf(menus));
        return menus;
    }

    public BbqMenuResponse getMenuDetail(Long menuId) {
        BbqMenuResponse menu = call(
            () -> restClient.get()
                .uri("/api/delivery/menu/detail/{menuId}", menuId)
                .retrieve()
                .body(BbqMenuResponse.class),
            "BBQ 메뉴 상세 조회"
        );
        log.info("BBQ 메뉴 상세 조회 성공: menuId={}, menuName={}", menuId, menu == null ? null : menu.getMenuName());
        return menu;
    }

    public List<BbqMenuSubOptionResponse> getMenuSubOptions(Long menuId) {
        List<BbqMenuSubOptionResponse> subOptions = call(
            () -> restClient.get()
                .uri("/api/delivery/menu/sub-option/{menuId}", menuId)
                .retrieve()
                .body(SUB_OPTION_LIST),
            "BBQ 메뉴 서브 옵션 조회"
        );
        log.info("BBQ 메뉴 서브 옵션 조회 성공: menuId={}, 옵션 수={}", menuId, sizeOf(subOptions));
        return subOptions;
    }

    private <T> T call(Supplier<T> request, String apiName) {
        try {
            return request.get();
        } catch (RestClientResponseException e) {
            log.error("{} 실패: Status={}, Message={}", apiName, e.getStatusCode(), e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("{} 중 예외 발생", apiName, e);
            throw e;
        }
    }

    private static int sizeOf(List<?> list) {
        return list == null ? 0 : list.size();
    }
}
