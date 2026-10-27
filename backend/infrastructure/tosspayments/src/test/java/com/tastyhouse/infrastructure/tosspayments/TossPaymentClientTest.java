package com.tastyhouse.infrastructure.tosspayments;

import java.net.SocketTimeoutException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

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
    private static final String LOOKUP_URL = BASE_URL + "/v1/payments/orders/order-1";
    private static final String CANCEL_URL = BASE_URL + "/v1/payments/pk-9/cancel";

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
            "/v1/payments/{paymentKey}/cancel",
            "/v1/payments/orders/{orderId}"
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
    @DisplayName("4xx 응답은 토스 본문의 code·message를 그대로 돌려준다")
    void confirmPaymentClientError() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"INVALID_REQUEST\",\"message\":\"잘못된 요청\"}"));

        TossPaymentConfirmResponse response = client.confirmPayment("pk-1", "order-1", 1000);

        assertThat(response.getCode()).isEqualTo("INVALID_REQUEST");
        assertThat(response.getMessage()).isEqualTo("잘못된 요청");
        server.verify();
    }

    @Test
    @DisplayName("4xx 응답 본문을 해석할 수 없으면 PG_UNREADABLE_RESPONSE 코드로 변환한다")
    void confirmPaymentClientErrorWithUnreadableBody() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.TEXT_PLAIN)
                .body("bad request"));

        TossPaymentConfirmResponse response = client.confirmPayment("pk-1", "order-1", 1000);

        assertThat(response.getCode()).isEqualTo("PG_UNREADABLE_RESPONSE");
        server.verify();
    }

    @Test
    @DisplayName("4xx 응답의 Content-Type이 JSON이어도 본문이 깨진 JSON이면 PG_UNREADABLE_RESPONSE 코드로 변환한다")
    void confirmPaymentClientErrorWithMalformedJsonBody() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"INVALID_REQUEST\",\"message\":"));

        TossPaymentConfirmResponse response = client.confirmPayment("pk-1", "order-1", 1000);

        assertThat(response.getCode()).isEqualTo("PG_UNREADABLE_RESPONSE");
        server.verify();
    }

    @Test
    @DisplayName("4xx JSON 응답에 code가 비어 있으면 PG_UNREADABLE_RESPONSE 코드로 변환한다")
    void confirmPaymentClientErrorWithBlankCode() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"message\":\"Too Many Requests\"}"));

        TossPaymentConfirmResponse response = client.confirmPayment("pk-1", "order-1", 1000);

        assertThat(response.getCode()).isEqualTo("PG_UNREADABLE_RESPONSE");
        server.verify();
    }

    @Test
    @DisplayName("프록시가 HTML 본문으로 돌려준 408 응답은 PG_UNREADABLE_RESPONSE 코드로 변환한다")
    void confirmPaymentRequestTimeoutWithHtmlBody() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withStatus(HttpStatus.REQUEST_TIMEOUT)
                .contentType(MediaType.TEXT_HTML)
                .body("<html><body>408 Request Time-out</body></html>"));

        TossPaymentConfirmResponse response = client.confirmPayment("pk-1", "order-1", 1000);

        assertThat(response.getCode()).isEqualTo("PG_UNREADABLE_RESPONSE");
        server.verify();
    }

    @Test
    @DisplayName("5xx 응답은 본문에 code가 있어도 PG_SERVER_ERROR 코드로 변환한다")
    void confirmPaymentServerError() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"FAILED_INTERNAL_SYSTEM_PROCESSING\",\"message\":\"내부 오류\"}"));

        TossPaymentConfirmResponse response = client.confirmPayment("pk-1", "order-1", 1000);

        assertThat(response.getCode()).isEqualTo("PG_SERVER_ERROR");
        server.verify();
    }

    @Test
    @DisplayName("응답을 받지 못한 타임아웃은 SYSTEM_ERROR 코드로 변환한다")
    void confirmPaymentTimeout() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(request -> {
                throw new SocketTimeoutException("Read timed out");
            });

        TossPaymentConfirmResponse response = client.confirmPayment("pk-1", "order-1", 1000);

        assertThat(response.getCode()).isEqualTo("SYSTEM_ERROR");
        server.verify();
    }

    @Test
    @DisplayName("주문번호 조회는 lookupPath의 orderId를 치환해 GET으로 보내고 결제 객체를 그대로 돌려준다")
    void lookupPaymentSuccess() {
        server.expect(requestTo(LOOKUP_URL))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Basic dGVzdC1zZWNyZXQ6"))
            .andRespond(withSuccess(
                "{\"paymentKey\":\"pk-1\",\"orderId\":\"order-1\",\"status\":\"DONE\",\"totalAmount\":1000}",
                MediaType.APPLICATION_JSON));

        TossPaymentConfirmResponse response = client.lookupPaymentByOrderId("order-1");

        assertThat(response.getCode()).isNull();
        assertThat(response.getStatus()).isEqualTo("DONE");
        assertThat(response.getTotalAmount()).isEqualTo(1000);
        server.verify();
    }

    @Test
    @DisplayName("주문번호 조회의 404 응답은 토스 본문의 code를 그대로 돌려준다")
    void lookupPaymentNotFound() {
        server.expect(requestTo(LOOKUP_URL))
            .andRespond(withStatus(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"NOT_FOUND_PAYMENT\",\"message\":\"존재하지 않는 결제 정보 입니다.\"}"));

        TossPaymentConfirmResponse response = client.lookupPaymentByOrderId("order-1");

        assertThat(response.getCode()).isEqualTo("NOT_FOUND_PAYMENT");
        server.verify();
    }

    @Test
    @DisplayName("주문번호 조회의 5xx 응답은 PG_SERVER_ERROR 코드로 변환한다")
    void lookupPaymentServerError() {
        server.expect(requestTo(LOOKUP_URL))
            .andRespond(withServerError());

        TossPaymentConfirmResponse response = client.lookupPaymentByOrderId("order-1");

        assertThat(response.getCode()).isEqualTo("PG_SERVER_ERROR");
        server.verify();
    }

    @Test
    @DisplayName("주문번호 조회의 4xx 응답 본문을 해석할 수 없으면 PG_UNREADABLE_RESPONSE 코드로 변환한다")
    void lookupPaymentClientErrorWithUnreadableBody() {
        server.expect(requestTo(LOOKUP_URL))
            .andRespond(withStatus(HttpStatus.REQUEST_TIMEOUT)
                .contentType(MediaType.TEXT_HTML)
                .body("<html><body>408 Request Time-out</body></html>"));

        TossPaymentConfirmResponse response = client.lookupPaymentByOrderId("order-1");

        assertThat(response.getCode()).isEqualTo("PG_UNREADABLE_RESPONSE");
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
    @DisplayName("취소 요청은 cancelPath의 paymentKey를 치환한 경로로 보내고 5xx는 PG_SERVER_ERROR로 변환한다")
    void cancelPaymentServerError() {
        server.expect(requestTo(CANCEL_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(jsonPath("$.cancelReason").value("고객 요청"))
            .andRespond(withServerError());

        TossPaymentConfirmResponse response = client.cancelPayment("pk-9", "고객 요청");

        assertThat(response.getCode()).isEqualTo("PG_SERVER_ERROR");
        server.verify();
    }

    @Test
    @DisplayName("취소의 400 이미 취소됨 응답은 토스 본문의 code·message를 그대로 돌려준다")
    void cancelPaymentAlreadyCanceled() {
        server.expect(requestTo(CANCEL_URL))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"ALREADY_CANCELED_PAYMENT\",\"message\":\"이미 취소된 결제 입니다.\"}"));

        TossPaymentConfirmResponse response = client.cancelPayment("pk-9", "고객 요청");

        assertThat(response.getCode()).isEqualTo("ALREADY_CANCELED_PAYMENT");
        assertThat(response.getMessage()).isEqualTo("이미 취소된 결제 입니다.");
        server.verify();
    }

    @Test
    @DisplayName("취소의 404 결제 없음 응답은 토스 본문의 code를 그대로 돌려준다")
    void cancelPaymentNotFound() {
        server.expect(requestTo(CANCEL_URL))
            .andRespond(withStatus(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"NOT_FOUND_PAYMENT\",\"message\":\"존재하지 않는 결제 정보 입니다.\"}"));

        TossPaymentConfirmResponse response = client.cancelPayment("pk-9", "고객 요청");

        assertThat(response.getCode()).isEqualTo("NOT_FOUND_PAYMENT");
        server.verify();
    }

    @Test
    @DisplayName("취소의 4xx 응답 본문을 해석할 수 없으면 PG_UNREADABLE_RESPONSE 코드로 변환한다")
    void cancelPaymentClientErrorWithUnreadableBody() {
        server.expect(requestTo(CANCEL_URL))
            .andRespond(withStatus(HttpStatus.REQUEST_TIMEOUT)
                .contentType(MediaType.TEXT_HTML)
                .body("<html><body>408 Request Time-out</body></html>"));

        TossPaymentConfirmResponse response = client.cancelPayment("pk-9", "고객 요청");

        assertThat(response.getCode()).isEqualTo("PG_UNREADABLE_RESPONSE");
        server.verify();
    }

    @Test
    @DisplayName("취소 응답을 받지 못한 타임아웃은 SYSTEM_ERROR 코드로 변환한다")
    void cancelPaymentTimeout() {
        server.expect(requestTo(CANCEL_URL))
            .andRespond(request -> {
                throw new SocketTimeoutException("Read timed out");
            });

        TossPaymentConfirmResponse response = client.cancelPayment("pk-9", "고객 요청");

        assertThat(response.getCode()).isEqualTo("SYSTEM_ERROR");
        server.verify();
    }
}
