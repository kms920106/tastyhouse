package com.tastyhouse.external.sms.solapi;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.tastyhouse.domain.sms.port.SmsSender;
import com.tastyhouse.external.exception.ExternalApiErrorCode;
import com.tastyhouse.external.exception.ExternalApiException;
import com.tastyhouse.external.sms.solapi.request.SolapiMessageRequest;
import com.tastyhouse.external.sms.solapi.response.SolapiMessageResponse;

@ConditionalOnProperty(name = "sms.provider", havingValue = "solapi", matchIfMissing = true)
@Component
public class SolapiSmsClient implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(SolapiSmsClient.class);

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String AUTH_SCHEME = "HMAC-SHA256";

    private final WebClient.Builder webClientBuilder;
    private final SolapiProperties solapiProperties;

    public SolapiSmsClient(WebClient.Builder webClientBuilder, SolapiProperties solapiProperties) {
        this.webClientBuilder = webClientBuilder;
        this.solapiProperties = solapiProperties;
    }

    @Override
    public void send(String to, String content) {
        SolapiMessageRequest request = new SolapiMessageRequest(
            List.of(new SolapiMessageRequest.SolapiMessage(
                to,
                solapiProperties.senderNumber(),
                content,
                "SMS",
                null
            ))
        );

        log.info("Solapi SMS 발송 요청. to: {}", to);

        try {
            String authorizationHeader = createAuthorizationHeader();

            SolapiMessageResponse response = webClientBuilder.build()
                .post()
                .uri(solapiProperties.baseUrl() + solapiProperties.sendManyPath())
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SolapiMessageResponse.class)
                .block();

            if (response == null) {
                log.warn("Solapi SMS 발송 응답 없음. to: {}", to);
                throw new ExternalApiException(ExternalApiErrorCode.SMS_SEND_NO_RESPONSE);
            }

            if (!response.isSuccess()) {
                log.error("Solapi SMS 발송 실패. to: {}, failedMessages: {}", to, response.getFailedMessageList());
                throw new ExternalApiException(ExternalApiErrorCode.SMS_SEND_FAILED);
            }

            log.info("Solapi SMS 발송 성공. to: {}", to);

        } catch (WebClientResponseException e) {
            log.error("Solapi SMS 발송 API 오류. status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new ExternalApiException(ExternalApiErrorCode.SMS_SEND_API_ERROR, e);
        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Solapi SMS 발송 중 예외 발생. to: {}", to, e);
            throw new ExternalApiException(ExternalApiErrorCode.SMS_SEND_API_ERROR, e);
        }
    }

    private String createAuthorizationHeader() throws Exception {
        String dateTime = Instant.now().toString();
        String salt = UUID.randomUUID().toString().replace("-", "");
        String signature = generateHmacSignature(solapiProperties.apiSecret(), dateTime, salt);

        return "%s apiKey=%s, date=%s, salt=%s, signature=%s".formatted(
            AUTH_SCHEME,
            solapiProperties.apiKey(),
            dateTime,
            salt,
            signature
        );
    }

    private String generateHmacSignature(String apiSecret, String dateTime, String salt) throws Exception {
        String message = dateTime + salt;
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(apiSecret.getBytes(), HMAC_ALGORITHM));
        byte[] rawHmac = mac.doFinal(message.getBytes());
        return bytesToHex(rawHmac);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
