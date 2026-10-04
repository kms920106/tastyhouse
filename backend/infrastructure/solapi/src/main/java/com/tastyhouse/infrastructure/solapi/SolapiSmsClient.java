package com.tastyhouse.infrastructure.solapi;

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
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.tastyhouse.application.sms.port.out.SmsSendFailure;
import com.tastyhouse.application.sms.port.out.SmsSendResult;
import com.tastyhouse.application.sms.port.out.SmsSender;
import com.tastyhouse.infrastructure.solapi.dto.SolapiMessageRequest;
import com.tastyhouse.infrastructure.solapi.dto.SolapiMessageResponse;

@ConditionalOnProperty(name = "sms.provider", havingValue = "solapi", matchIfMissing = true)
@Component
public class SolapiSmsClient implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(SolapiSmsClient.class);

    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String AUTH_SCHEME = "HMAC-SHA256";

    private final RestClient restClient;
    private final SolapiProperties solapiProperties;

    public SolapiSmsClient(RestClient.Builder restClientBuilder, SolapiProperties solapiProperties) {
        this.restClient = restClientBuilder.baseUrl(solapiProperties.baseUrl()).build();
        this.solapiProperties = solapiProperties;
    }

    @Override
    public SmsSendResult send(String to, String content) {
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

            SolapiMessageResponse response = restClient
                .post()
                .uri(solapiProperties.sendManyPath())
                .header("Authorization", authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(SolapiMessageResponse.class);

            if (response == null) {
                log.warn("Solapi SMS 발송 응답 없음. to: {}", to);
                return SmsSendResult.failed(SmsSendFailure.NO_RESPONSE);
            }

            if (!response.isSuccess()) {
                log.error("Solapi SMS 발송 실패. to: {}, failedMessages: {}", to, response.getFailedMessageList());
                return SmsSendResult.failed(SmsSendFailure.FAILED);
            }

            log.info("Solapi SMS 발송 성공. to: {}", to);
            return SmsSendResult.sent();
        } catch (RestClientResponseException e) {
            log.error("Solapi SMS 발송 API 오류. status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return SmsSendResult.failed(SmsSendFailure.API_ERROR, e);
        } catch (Exception e) {
            log.error("Solapi SMS 발송 중 예외 발생. to: {}", to, e);
            return SmsSendResult.failed(SmsSendFailure.API_ERROR, e);
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
