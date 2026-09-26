package com.tastyhouse.external.payment.toss;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.tastyhouse.external.payment.toss.dto.TossPaymentConfirmResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TossPaymentClientTest {

    private static final String BASE_URL = "https://pg.test";
    private static final String CONFIRM_URL = BASE_URL + "/v1/payments/confirm";

    private MockRestServiceServer server;
    private TossPaymentClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        TossPaymentProperties properties = new TossPaymentProperties(
            "test-secret",
            BASE_URL,
            "/v1/payments/confirm",
            "/v1/payments/{paymentKey}/cancel"
        );
        client = new TossPaymentClient(builder, properties);
    }

    @Test
    @DisplayName("승인 요청은 baseUrl과 confirmPath로 Basic 인증 헤더와 함께 보내고 성공 응답을 그대로 돌려준다")
    void confirmPaymentSuccess() {
        server.expect(requestTo(CONFIRM_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dGVzdC1zZWNyZXQ6"))
            .andExpect(jsonPath("$.paymentKey").value("pk-1"))
            .andExpect(jsonPath("$.orderId").value("order-1"))
            .andExpect(jsonPath("$.amount").value(1000))
            .andRespond(withSuccess("{\"paymentKey\":\"pk-1\",\"status\":\"DONE\"}", MediaType.APPLICATION_JSON));

        TossPaymentConfirmResponse response = client.confirmPayment("pk-1", "order-1", 1000);

        assertThat(response.getStatus()).isEqualTo("DONE");
        assertThat(response.getCode()).isNull();
        server.verify();
    }

    @Test
    @DisplayName("4xx 응답은 PG_API_ERROR 코드로 변환한다")
    void confirmPaymentClientError() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"INVALID_REQUEST\",\"message\":\"잘못된 요청\"}"));

        TossPaymentConfirmResponse response = client.confirmPayment("pk-1", "order-1", 1000);

        assertThat(response.getCode()).isEqualTo("PG_API_ERROR");
        server.verify();
    }

    @Test
    @DisplayName("본문이 없는 성공 응답은 UNKNOWN_ERROR 코드로 변환한다")
    void confirmPaymentEmptyBody() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withSuccess());

        TossPaymentConfirmResponse response = client.confirmPayment("pk-1", "order-1", 1000);

        assertThat(response.getCode()).isEqualTo("UNKNOWN_ERROR");
        server.verify();
    }

    @Test
    @DisplayName("취소 요청은 cancelPath의 paymentKey를 치환한 경로로 보내고 5xx는 PG_API_ERROR로 변환한다")
    void cancelPaymentServerError() {
        server.expect(requestTo(BASE_URL + "/v1/payments/pk-9/cancel"))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.cancelReason").value("고객 요청"))
            .andRespond(withServerError());

        TossPaymentConfirmResponse response = client.cancelPayment("pk-9", "고객 요청");

        assertThat(response.getCode()).isEqualTo("PG_API_ERROR");
        server.verify();
    }
}
