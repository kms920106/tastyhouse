package com.tastyhouse.external.solapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.tastyhouse.application.sms.port.out.SmsSendFailure;
import com.tastyhouse.application.sms.port.out.SmsSendResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class SolapiSmsClientTest {

    private static final String BASE_URL = "https://sms.test";
    private static final String SEND_URL = BASE_URL + "/messages/v4/send-many/detail";

    private MockRestServiceServer server;
    private SolapiSmsClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        SolapiProperties properties = new SolapiProperties(
            "api-key",
            "api-secret",
            "01000000000",
            BASE_URL,
            "/messages/v4/send-many/detail"
        );
        client = new SolapiSmsClient(builder, properties);
    }

    @Test
    @DisplayName("실패 목록이 없는 응답이면 성공 결과를 돌려준다")
    void sendSuccess() {
        server.expect(requestTo(SEND_URL))
            .andExpect(method(HttpMethod.POST))
            .andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("HMAC-SHA256 apiKey=api-key, date=")))
            .andExpect(jsonPath("$.messages[0].to").value("01012345678"))
            .andExpect(jsonPath("$.messages[0].from").value("01000000000"))
            .andExpect(jsonPath("$.messages[0].subject").doesNotExist())
            .andRespond(withSuccess("{\"failedMessageList\":[]}", MediaType.APPLICATION_JSON));

        SmsSendResult result = client.send("01012345678", "인증번호 123456");

        assertThat(result.success()).isTrue();
        server.verify();
    }

    @Test
    @DisplayName("실패 목록이 있으면 FAILED 결과를 돌려준다")
    void sendFailedMessages() {
        server.expect(requestTo(SEND_URL))
            .andRespond(withSuccess(
                "{\"failedMessageList\":[{\"to\":\"01012345678\",\"statusCode\":\"3059\"}]}",
                MediaType.APPLICATION_JSON
            ));

        SmsSendResult result = client.send("01012345678", "인증번호 123456");

        assertThat(result.failure()).isEqualTo(SmsSendFailure.FAILED);
        assertThat(result.cause()).isNull();
        server.verify();
    }

    @Test
    @DisplayName("본문이 없는 응답은 NO_RESPONSE 결과를 돌려준다")
    void sendEmptyBody() {
        server.expect(requestTo(SEND_URL))
            .andRespond(withSuccess());

        SmsSendResult result = client.send("01012345678", "인증번호 123456");

        assertThat(result.failure()).isEqualTo(SmsSendFailure.NO_RESPONSE);
        assertThat(result.cause()).isNull();
        server.verify();
    }

    @Test
    @DisplayName("5xx 응답은 원인 예외를 담은 API_ERROR 결과를 돌려준다")
    void sendServerError() {
        server.expect(requestTo(SEND_URL))
            .andRespond(withServerError());

        SmsSendResult result = client.send("01012345678", "인증번호 123456");

        assertThat(result.failure()).isEqualTo(SmsSendFailure.API_ERROR);
        assertThat(result.cause()).isNotNull();
        server.verify();
    }
}
