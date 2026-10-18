package com.tastyhouse.infrastructure.tosspayments;

import java.net.SocketTimeoutException;
import java.util.List;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.tastyhouse.application.payment.port.out.PgConfirmFailure;
import com.tastyhouse.application.payment.port.out.PgConfirmResult;
import com.tastyhouse.application.payment.port.out.PgPaymentLookupResult;
import com.tastyhouse.application.payment.port.out.PgPaymentLookupStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class TossPaymentGatewayAdapterTest {

    private static final String BASE_URL = "https://pg.test";
    private static final String CONFIRM_URL = BASE_URL + "/v1/payments/confirm";
    private static final String LOOKUP_URL = BASE_URL + "/v1/payments/orders/order-1";

    private MockRestServiceServer server;
    private TossPaymentGatewayAdapter adapter;
    private Logger adapterLogger;
    private ListAppender<ILoggingEvent> adapterLogs;

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
        adapter = new TossPaymentGatewayAdapter(new TossPaymentClient(builder, properties));
        adapterLogger = (Logger) LoggerFactory.getLogger(TossPaymentGatewayAdapter.class);
        adapterLogs = new ListAppender<>();
        adapterLogs.start();
        adapterLogger.addAppender(adapterLogs);
    }

    @AfterEach
    void detachLogCapture() {
        adapterLogger.detachAppender(adapterLogs);
        adapterLogs.stop();
    }

    @Test
    @DisplayName("이미 처리된 결제라는 4xx 응답은 ALREADY_PROCESSED 실패로 번역한다")
    void translatesAlreadyProcessedPayment() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"ALREADY_PROCESSED_PAYMENT\",\"message\":\"이미 처리된 결제 입니다.\"}"));

        PgConfirmResult result = adapter.confirmPayment(1L, "pk-1", "order-1", 1000);

        assertThat(result.success()).isFalse();
        assertThat(result.failure()).isEqualTo(PgConfirmFailure.ALREADY_PROCESSED);
        assertThat(result.errorCode()).isEqualTo("ALREADY_PROCESSED_PAYMENT");
        assertThat(result.errorMessage()).isEqualTo("이미 처리된 결제 입니다.");
        assertThat(adapterLogLevels()).containsExactly(Level.WARN);
        server.verify();
    }

    @Test
    @DisplayName("토스 JSON 본문을 가진 그 밖의 4xx 응답은 REJECTED 실패로 번역한다")
    void translatesOtherErrorsAsRejected() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"REJECT_CARD_COMPANY\",\"message\":\"카드사 거절\"}"));

        PgConfirmResult result = adapter.confirmPayment(1L, "pk-1", "order-1", 1000);

        assertThat(result.success()).isFalse();
        assertThat(result.failure()).isEqualTo(PgConfirmFailure.REJECTED);
        assertThat(adapterLogLevels()).containsExactly(Level.ERROR);
        server.verify();
    }

    @Test
    @DisplayName("승인 가능 시한이 지난 EXPIRED_PAYMENT 400 응답은 승인된 적이 없으므로 REJECTED 실패로 번역한다")
    void translatesExpiredPaymentAsRejected() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"EXPIRED_PAYMENT\",\"message\":\"결제가 만료되었습니다. 다시 시도해주세요.\"}"));

        PgConfirmResult result = adapter.confirmPayment(1L, "pk-1", "order-1", 1000);

        assertThat(result.success()).isFalse();
        assertThat(result.failure()).isEqualTo(PgConfirmFailure.REJECTED);
        assertThat(result.errorCode()).isEqualTo("EXPIRED_PAYMENT");
        server.verify();
    }

    @Test
    @DisplayName("본문을 해석할 수 없는 승인 4xx 응답은 토스가 처리했는지 알 수 없으므로 UNKNOWN 실패로 번역한다")
    void translatesUnreadableClientErrorAsUnknown() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withStatus(HttpStatus.REQUEST_TIMEOUT)
                .contentType(MediaType.TEXT_HTML)
                .body("<html><body>408 Request Time-out</body></html>"));

        PgConfirmResult result = adapter.confirmPayment(1L, "pk-1", "order-1", 1000);

        assertThat(result.success()).isFalse();
        assertThat(result.failure()).isEqualTo(PgConfirmFailure.UNKNOWN);
        assertThat(result.errorCode()).isEqualTo("PG_UNREADABLE_RESPONSE");
        server.verify();
    }

    @Test
    @DisplayName("승인 성공 응답은 실패 사유를 갖지 않는다")
    void successHasNoFailure() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withSuccess(
                "{\"paymentKey\":\"pk-1\",\"status\":\"DONE\",\"totalAmount\":1000}", MediaType.APPLICATION_JSON));

        PgConfirmResult result = adapter.confirmPayment(1L, "pk-1", "order-1", 1000);

        assertThat(result.success()).isTrue();
        assertThat(result.failure()).isNull();
        assertThat(adapterLogLevels()).isEmpty();
        server.verify();
    }

    @Test
    @DisplayName("승인 5xx 응답은 결과를 알 수 없으므로 UNKNOWN 실패로 번역한다")
    void translatesServerErrorAsUnknown() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withServerError());

        PgConfirmResult result = adapter.confirmPayment(1L, "pk-1", "order-1", 1000);

        assertThat(result.success()).isFalse();
        assertThat(result.failure()).isEqualTo(PgConfirmFailure.UNKNOWN);
        assertThat(result.errorCode()).isEqualTo("PG_SERVER_ERROR");
        server.verify();
    }

    @Test
    @DisplayName("승인 호출 예외(SYSTEM_ERROR)와 빈 응답(UNKNOWN_ERROR)도 UNKNOWN 실패로 번역한다")
    void translatesSystemAndEmptyResponseAsUnknown() {
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(request -> {
                throw new SocketTimeoutException("Read timed out");
            });
        server.expect(requestTo(CONFIRM_URL))
            .andRespond(withSuccess());

        PgConfirmResult timeout = adapter.confirmPayment(1L, "pk-1", "order-1", 1000);
        PgConfirmResult empty = adapter.confirmPayment(1L, "pk-1", "order-1", 1000);

        assertThat(timeout.failure()).isEqualTo(PgConfirmFailure.UNKNOWN);
        assertThat(timeout.errorCode()).isEqualTo("SYSTEM_ERROR");
        assertThat(empty.failure()).isEqualTo(PgConfirmFailure.UNKNOWN);
        assertThat(empty.errorCode()).isEqualTo("UNKNOWN_ERROR");
        server.verify();
    }

    @Test
    @DisplayName("조회 결과가 DONE이면 APPROVED와 성공 결과(paymentKey·금액)를 돌려준다")
    void lookupDoneIsApproved() {
        server.expect(requestTo(LOOKUP_URL))
            .andRespond(withSuccess(
                "{\"paymentKey\":\"pk-1\",\"orderId\":\"order-1\",\"status\":\"DONE\",\"totalAmount\":1000}",
                MediaType.APPLICATION_JSON));

        PgPaymentLookupResult lookup = adapter.lookupPayment("order-1");

        assertThat(lookup.status()).isEqualTo(PgPaymentLookupStatus.APPROVED);
        assertThat(lookup.result().success()).isTrue();
        assertThat(lookup.result().paymentKey()).isEqualTo("pk-1");
        assertThat(lookup.result().totalAmount()).isEqualTo(1000);
        assertThat(lookup.result().failure()).isNull();
        server.verify();
    }

    @Test
    @DisplayName("조회 응답의 orderId가 요청과 다르면 DONE이어도 믿지 않고 UNKNOWN이다")
    void lookupOrderIdMismatchIsUnknown() {
        server.expect(requestTo(LOOKUP_URL))
            .andRespond(withSuccess(
                "{\"paymentKey\":\"pk-9\",\"orderId\":\"order-9\",\"status\":\"DONE\",\"totalAmount\":1000}",
                MediaType.APPLICATION_JSON));

        PgPaymentLookupResult lookup = adapter.lookupPayment("order-1");

        assertThat(lookup.status()).isEqualTo(PgPaymentLookupStatus.UNKNOWN);
        assertThat(lookup.result().success()).isFalse();
        assertThat(lookup.result().failure()).isEqualTo(PgConfirmFailure.UNKNOWN);
        assertThat(lookup.result().errorCode()).isEqualTo("ORDER_ID_MISMATCH");
        assertThat(lookup.result().detail()).isNull();
        assertThat(adapterLogLevels()).contains(Level.ERROR);
        server.verify();
    }

    @Test
    @DisplayName("조회 결과가 IN_PROGRESS면 승인 처리 중일 수 있으므로 UNKNOWN이다")
    void lookupInProgressIsUnknown() {
        server.expect(requestTo(LOOKUP_URL))
            .andRespond(withSuccess("{\"orderId\":\"order-1\",\"status\":\"IN_PROGRESS\"}", MediaType.APPLICATION_JSON));

        PgPaymentLookupResult lookup = adapter.lookupPayment("order-1");

        assertThat(lookup.status()).isEqualTo(PgPaymentLookupStatus.UNKNOWN);
        assertThat(lookup.result().success()).isFalse();
        assertThat(lookup.result().failure()).isEqualTo(PgConfirmFailure.UNKNOWN);
        server.verify();
    }

    @Test
    @DisplayName("조회 결과가 ABORTED면 NOT_APPROVED이고 실패 사유는 REJECTED다")
    void lookupAbortedIsNotApproved() {
        server.expect(requestTo(LOOKUP_URL))
            .andRespond(withSuccess("{\"orderId\":\"order-1\",\"status\":\"ABORTED\"}", MediaType.APPLICATION_JSON));

        PgPaymentLookupResult lookup = adapter.lookupPayment("order-1");

        assertThat(lookup.status()).isEqualTo(PgPaymentLookupStatus.NOT_APPROVED);
        assertThat(lookup.result().failure()).isEqualTo(PgConfirmFailure.REJECTED);
        server.verify();
    }

    @Test
    @DisplayName("조회 404라도 코드가 NOT_FOUND_PAYMENT가 아니면 결제가 없다고 단정하지 않고 UNKNOWN이다")
    void lookupNotFoundOtherCodeIsUnknown() {
        server.expect(requestTo(LOOKUP_URL))
            .andRespond(withStatus(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"NOT_FOUND\",\"message\":\"요청한 리소스를 찾을 수 없습니다.\"}"));

        PgPaymentLookupResult lookup = adapter.lookupPayment("order-1");

        assertThat(lookup.status()).isEqualTo(PgPaymentLookupStatus.UNKNOWN);
        assertThat(lookup.result().failure()).isEqualTo(PgConfirmFailure.UNKNOWN);
        assertThat(lookup.result().errorCode()).isEqualTo("NOT_FOUND");
        server.verify();
    }

    @Test
    @DisplayName("조회 404 NOT_FOUND_PAYMENT는 결제가 만들어지지 않은 것이므로 NOT_APPROVED다")
    void lookupNotFoundIsNotApproved() {
        server.expect(requestTo(LOOKUP_URL))
            .andRespond(withStatus(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"code\":\"NOT_FOUND_PAYMENT\",\"message\":\"존재하지 않는 결제 정보 입니다.\"}"));

        PgPaymentLookupResult lookup = adapter.lookupPayment("order-1");

        assertThat(lookup.status()).isEqualTo(PgPaymentLookupStatus.NOT_APPROVED);
        assertThat(lookup.result().failure()).isEqualTo(PgConfirmFailure.REJECTED);
        assertThat(lookup.result().errorCode()).isEqualTo("NOT_FOUND_PAYMENT");
        server.verify();
    }

    @Test
    @DisplayName("조회도 5xx면 UNKNOWN이다")
    void lookupServerErrorIsUnknown() {
        server.expect(requestTo(LOOKUP_URL))
            .andRespond(withServerError());

        PgPaymentLookupResult lookup = adapter.lookupPayment("order-1");

        assertThat(lookup.status()).isEqualTo(PgPaymentLookupStatus.UNKNOWN);
        assertThat(lookup.result().failure()).isEqualTo(PgConfirmFailure.UNKNOWN);
        assertThat(lookup.result().errorCode()).isEqualTo("PG_SERVER_ERROR");
        server.verify();
    }

    @Test
    @DisplayName("조회도 본문을 해석할 수 없는 4xx면 UNKNOWN이다")
    void lookupUnreadableClientErrorIsUnknown() {
        server.expect(requestTo(LOOKUP_URL))
            .andRespond(withStatus(HttpStatus.REQUEST_TIMEOUT)
                .contentType(MediaType.TEXT_HTML)
                .body("<html><body>408 Request Time-out</body></html>"));

        PgPaymentLookupResult lookup = adapter.lookupPayment("order-1");

        assertThat(lookup.status()).isEqualTo(PgPaymentLookupStatus.UNKNOWN);
        assertThat(lookup.result().failure()).isEqualTo(PgConfirmFailure.UNKNOWN);
        assertThat(lookup.result().errorCode()).isEqualTo("PG_UNREADABLE_RESPONSE");
        server.verify();
    }

    @Test
    @DisplayName("조회 결과가 READY면 승인 직후 아직 전이 전일 수 있으므로 UNKNOWN이다")
    void lookupReadyIsUnknown() {
        assertLookupStatus("READY", PgPaymentLookupStatus.UNKNOWN, PgConfirmFailure.UNKNOWN);
    }

    @Test
    @DisplayName("조회 결과가 EXPIRED면 NOT_APPROVED이고 실패 사유는 REJECTED다")
    void lookupExpiredIsNotApproved() {
        assertLookupStatus("EXPIRED", PgPaymentLookupStatus.NOT_APPROVED, PgConfirmFailure.REJECTED);
    }

    @Test
    @DisplayName("조회 결과가 CANCELED면 돈이 돌아간 상태라 NOT_APPROVED이고 실패 사유는 REJECTED다")
    void lookupCanceledIsNotApproved() {
        assertLookupStatus("CANCELED", PgPaymentLookupStatus.NOT_APPROVED, PgConfirmFailure.REJECTED);
    }

    @Test
    @DisplayName("조회 결과가 PARTIAL_CANCELED면 승인 뒤 일부 취소된 상태라 UNKNOWN이다")
    void lookupPartialCanceledIsUnknown() {
        assertLookupStatus("PARTIAL_CANCELED", PgPaymentLookupStatus.UNKNOWN, PgConfirmFailure.UNKNOWN);
    }

    private void assertLookupStatus(String tossStatus, PgPaymentLookupStatus expectedStatus, PgConfirmFailure expectedFailure) {
        server.expect(requestTo(LOOKUP_URL))
            .andRespond(withSuccess("{\"orderId\":\"order-1\",\"status\":\"" + tossStatus + "\"}", MediaType.APPLICATION_JSON));

        PgPaymentLookupResult lookup = adapter.lookupPayment("order-1");

        assertThat(lookup.status()).isEqualTo(expectedStatus);
        assertThat(lookup.result().success()).isFalse();
        assertThat(lookup.result().failure()).isEqualTo(expectedFailure);
        assertThat(lookup.result().errorCode()).isEqualTo(tossStatus);
        server.verify();
    }

    private List<Level> adapterLogLevels() {
        return adapterLogs.list.stream().map(ILoggingEvent::getLevel).toList();
    }
}
