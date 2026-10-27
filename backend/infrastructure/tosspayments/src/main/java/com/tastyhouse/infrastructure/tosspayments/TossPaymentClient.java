package com.tastyhouse.infrastructure.tosspayments;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
class TossPaymentClient {

    private static final Logger log = LoggerFactory.getLogger(TossPaymentClient.class);

    static final String PG_SERVER_ERROR = "PG_SERVER_ERROR";
    static final String SYSTEM_ERROR = "SYSTEM_ERROR";
    static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
    static final String PG_UNREADABLE_RESPONSE = "PG_UNREADABLE_RESPONSE";

    private final RestClient restClient;
    private final TossPaymentProperties tossPaymentProperties;

    public TossPaymentClient(RestClient.Builder restClientBuilder, TossPaymentProperties tossPaymentProperties) {
        this.restClient = restClientBuilder.baseUrl(tossPaymentProperties.baseUrl()).build();
        this.tossPaymentProperties = tossPaymentProperties;
    }

    public TossPaymentConfirmResponse confirmPayment(String paymentKey, String pgOrderId, Integer amount) {
        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(paymentKey, amount, pgOrderId);

        log.info("토스 결제 승인하기 API 요청. paymentKey: {}, pgOrderId: {}, amount: {}", paymentKey, pgOrderId, amount);

        try {
            TossPaymentConfirmResponse response = restClient
                .post()
                .uri(tossPaymentProperties.confirmPath())
                .header(HttpHeaders.AUTHORIZATION, createAuthorizationHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TossPaymentConfirmResponse.class);

            if (response == null) {
                log.warn("토스 결제 승인하기 API 응답 없음. paymentKey: {}", paymentKey);
                return errorResponse(UNKNOWN_ERROR, "응답이 없습니다.");
            }

            log.info("토스 결제 승인하기 API 완료. paymentKey: {}, status: {}", paymentKey, response.getStatus());
            return response;

        } catch (RestClientResponseException e) {
            return translateResponseError("결제 승인하기", e);
        } catch (Exception e) {
            log.error("토스 결제 승인하기 API 에러. error: ", e);
            return errorResponse(SYSTEM_ERROR, "결제 승인 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public TossPaymentConfirmResponse lookupPaymentByOrderId(String pgOrderId) {
        log.info("토스 주문번호로 결제 조회하기 API 요청. pgOrderId: {}", pgOrderId);

        try {
            TossPaymentConfirmResponse response = restClient
                .get()
                .uri(tossPaymentProperties.lookupPath(), pgOrderId)
                .header(HttpHeaders.AUTHORIZATION, createAuthorizationHeader())
                .retrieve()
                .body(TossPaymentConfirmResponse.class);

            if (response == null) {
                log.warn("토스 주문번호로 결제 조회하기 API 응답 없음. pgOrderId: {}", pgOrderId);
                return errorResponse(UNKNOWN_ERROR, "응답이 없습니다.");
            }

            log.info("토스 주문번호로 결제 조회하기 API 완료. pgOrderId: {}, status: {}", pgOrderId, response.getStatus());
            return response;

        } catch (RestClientResponseException e) {
            return translateResponseError("주문번호로 결제 조회하기", e);
        } catch (Exception e) {
            log.error("토스 주문번호로 결제 조회하기 API 에러. error: ", e);
            return errorResponse(SYSTEM_ERROR, "결제 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public TossPaymentConfirmResponse cancelPayment(String paymentKey, String cancelReason) {
        TossPaymentCancelRequest request = new TossPaymentCancelRequest(cancelReason);

        String cancelUrl = tossPaymentProperties.cancelPath().replace("{paymentKey}", paymentKey);

        log.info("토스 전액 취소하기 API 요청. paymentKey: {}, cancelReason: {}", paymentKey, cancelReason);

        try {
            TossPaymentConfirmResponse response = restClient
                .post()
                .uri(cancelUrl)
                .header(HttpHeaders.AUTHORIZATION, createAuthorizationHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(TossPaymentConfirmResponse.class);

            if (response == null) {
                log.warn("토스 전액 취소하기 API 응답 없음. paymentKey: {}", paymentKey);
                return errorResponse(UNKNOWN_ERROR, "응답이 없습니다.");
            }

            log.info("토스 전액 취소하기 API 완료. paymentKey: {}, status: {}", paymentKey, response.getStatus());
            return response;
        } catch (RestClientResponseException e) {
            return translateResponseError("전액 취소하기", e);
        } catch (Exception e) {
            log.error("토스 전액 취소하기 API 에러. error: ", e);
            return errorResponse(SYSTEM_ERROR, "결제 취소 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private String createAuthorizationHeader() {
        String credentials = tossPaymentProperties.secretKey() + ":";
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedCredentials;
    }

    private TossPaymentConfirmResponse translateResponseError(String apiName, RestClientResponseException e) {
        if (e.getStatusCode().is4xxClientError()) {
            log.warn("토스 {} API 4xx 응답. status: {}, body: {}", apiName, e.getStatusCode(), e.getResponseBodyAsString());
            TossPaymentConfirmResponse tossError = readErrorBody(apiName, e);
            if (tossError != null) {
                return tossError;
            }
            return errorResponse(PG_UNREADABLE_RESPONSE, apiName + " API 호출에 실패했습니다: " + e.getMessage());
        }
        log.error("토스 {} API 실패. status: {}, body: {}", apiName, e.getStatusCode(), e.getResponseBodyAsString(), e);
        return errorResponse(PG_SERVER_ERROR, apiName + " API 호출에 실패했습니다: " + e.getMessage());
    }

    private TossPaymentConfirmResponse readErrorBody(String apiName, RestClientResponseException e) {
        try {
            TossPaymentConfirmResponse body = e.getResponseBodyAs(TossPaymentConfirmResponse.class);
            if (body == null || body.getCode() == null || body.getCode().isBlank()) {
                return null;
            }
            return body;
        } catch (RestClientException | IllegalStateException parseFailure) {
            log.warn("토스 {} API 오류 본문 해석 실패. status: {}", apiName, e.getStatusCode(), parseFailure);
            return null;
        }
    }

    private static TossPaymentConfirmResponse errorResponse(String code, String message) {
        TossPaymentConfirmResponse errorResponse = new TossPaymentConfirmResponse();
        errorResponse.setCode(code);
        errorResponse.setMessage(message);
        return errorResponse;
    }
}
