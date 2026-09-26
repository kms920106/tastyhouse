package com.tastyhouse.external.bbq;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.tastyhouse.external.bbq.dto.BbqMenuCategoryResponse;
import com.tastyhouse.external.bbq.dto.BbqMenuResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BbqApiClientTest {

    private static final String BASE_URL = "https://bbq.test";

    private MockRestServiceServer server;
    private BbqApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new BbqApiClient(builder, new BbqProperties(BASE_URL));
    }

    @Test
    @DisplayName("카테고리 목록 JSON 배열을 응답 DTO 목록으로 역직렬화한다")
    void getMenuCategories() {
        server.expect(requestTo(BASE_URL + "/api/delivery/menu/category"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(
                "[{\"id\":1,\"categoryName\":\"치킨\",\"priority\":1,\"isFullSize\":true},"
                    + "{\"id\":2,\"categoryName\":\"사이드\",\"priority\":2,\"isFullSize\":false}]",
                MediaType.APPLICATION_JSON
            ));

        List<BbqMenuCategoryResponse> categories = client.getMenuCategories();

        assertThat(categories).extracting(BbqMenuCategoryResponse::getCategoryName)
            .containsExactly("치킨", "사이드");
        assertThat(categories.get(0).getFullSize()).isTrue();
        server.verify();
    }

    @Test
    @DisplayName("메뉴 상세는 menuId를 경로에 넣어 단건 DTO로 역직렬화한다")
    void getMenuDetail() {
        server.expect(requestTo(BASE_URL + "/api/delivery/menu/detail/77"))
            .andRespond(withSuccess("{\"id\":77,\"menuName\":\"황금올리브\"}", MediaType.APPLICATION_JSON));

        BbqMenuResponse menu = client.getMenuDetail(77L);

        assertThat(menu.getMenuName()).isEqualTo("황금올리브");
        server.verify();
    }

    @Test
    @DisplayName("오류 응답은 RestClientResponseException으로 전파한다")
    void propagatesErrorResponse() {
        server.expect(requestTo(BASE_URL + "/api/delivery/menu/3"))
            .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> client.getMenusByCategoryId(3L))
            .isInstanceOf(RestClientResponseException.class);
        server.verify();
    }
}
