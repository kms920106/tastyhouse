package com.tastyhouse.external.payment.toss;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.tastyhouse.external.payment.toss.dto.TossPaymentCancelRequest;
import com.tastyhouse.external.payment.toss.dto.TossPaymentConfirmRequest;
import com.tastyhouse.external.payment.toss.dto.TossPaymentConfirmResponse;

@Component
public class TossPaymentClient {

    private static final Logger log = LoggerFactory.getLogger(TossPaymentClient.class);

    private final WebClient.Builder webClientBuilder;
    private final TossPaymentProperties tossPaymentProperties;

    private WebClient webClient;

    public TossPaymentClient(WebClient.Builder webClientBuilder, TossPaymentProperties tossPaymentProperties) {
        this.webClientBuilder = webClientBuilder;
        this.tossPaymentProperties = tossPaymentProperties;
    }

    @PostConstruct
    private void init() {
        this.webClient = webClientBuilder
            .baseUrl(tossPaymentProperties.baseUrl())
            .build();
    }

    public TossPaymentConfirmResponse confirmPayment(String paymentKey, String pgOrderId, Integer amount) {
        TossPaymentConfirmRequest request = new TossPaymentConfirmRequest(paymentKey, amount, pgOrderId);

        log.info("토스 결제 승인하기 API 요청. paymentKey: {}, pgOrderId: {}, amount: {}", paymentKey, pgOrderId, amount);

        try {
            TossPaymentConfirmResponse response = webClient
                .post()
                .uri(tossPaymentProperties.confirmPath())
                .header(HttpHeaders.AUTHORIZATION, createAuthorizationHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TossPaymentConfirmResponse.class)
                .block();

            if (response == null) {
                log.warn("토스 결제 승인하기 API 응답 없음. paymentKey: {}", paymentKey);
                TossPaymentConfirmResponse errorResponse = new TossPaymentConfirmResponse();
                errorResponse.setCode("UNKNOWN_ERROR");
                errorResponse.setMessage("응답이 없습니다.");
                return errorResponse;
            }

            log.info("토스 결제 승인하기 API 완료. paymentKey: {}, status: {}", paymentKey, response.getStatus());
            return response;

        } catch (WebClientResponseException e) {
            log.error("토스 결제 승인하기 API 실패. status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            TossPaymentConfirmResponse errorResponse = new TossPaymentConfirmResponse();
            errorResponse.setCode("PG_API_ERROR");
            errorResponse.setMessage("결제 승인하기 API 호출에 실패했습니다: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            log.error("토스 결제 승인하기 API 에러. error: ", e);
            TossPaymentConfirmResponse errorResponse = new TossPaymentConfirmResponse();
            errorResponse.setCode("SYSTEM_ERROR");
            errorResponse.setMessage("결제 승인 중 오류가 발생했습니다: " + e.getMessage());
            return errorResponse;
        }
    }

    public TossPaymentConfirmResponse cancelPayment(String paymentKey, String cancelReason) {
        TossPaymentCancelRequest request = new TossPaymentCancelRequest(cancelReason);

        String cancelUrl = tossPaymentProperties.cancelPath().replace("{paymentKey}", paymentKey);

        log.info("토스 전액 취소하기 API 요청. paymentKey: {}, cancelReason: {}", paymentKey, cancelReason);

        try {
            TossPaymentConfirmResponse response = webClient
                .post()
                .uri(cancelUrl)
                .header(HttpHeaders.AUTHORIZATION, createAuthorizationHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(TossPaymentConfirmResponse.class)
                .block();

            if (response == null) {
                log.warn("토스 전액 취소하기 API 응답 없음. paymentKey: {}", paymentKey);
                TossPaymentConfirmResponse errorResponse = new TossPaymentConfirmResponse();
                errorResponse.setCode("UNKNOWN_ERROR");
                errorResponse.setMessage("응답이 없습니다.");
                return errorResponse;
            }

            log.info("토스 전액 취소하기 API 완료. paymentKey: {}, status: {}", paymentKey, response.getStatus());
            return response;
        } catch (WebClientResponseException e) {
            log.error("토스 전액 취소하기 API 실패. status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            TossPaymentConfirmResponse errorResponse = new TossPaymentConfirmResponse();
            errorResponse.setCode("PG_API_ERROR");
            errorResponse.setMessage("결제 취소 API 호출에 실패했습니다: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            log.error("토스 전액 취소하기 API 에러. error: ", e);
            TossPaymentConfirmResponse errorResponse = new TossPaymentConfirmResponse();
            errorResponse.setCode("SYSTEM_ERROR");
            errorResponse.setMessage("결제 취소 중 오류가 발생했습니다: " + e.getMessage());
            return errorResponse;
        }
    }

    private String createAuthorizationHeader() {
        String credentials = tossPaymentProperties.secretKey() + ":";
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedCredentials;
    }
}
