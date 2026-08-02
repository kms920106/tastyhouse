package com.tastyhouse.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.tastyhouse.external.crawling.bbq.BbqProperties;
import com.tastyhouse.external.file.FileStorageProperties;
import com.tastyhouse.external.file.firebase.FirebaseStorageProperties;
import com.tastyhouse.external.file.s3.S3FileStorageProperties;
import com.tastyhouse.external.mail.MailProperties;
import com.tastyhouse.external.payment.toss.TossPaymentProperties;
import com.tastyhouse.external.sms.SmsProperties;
import com.tastyhouse.external.sms.solapi.SolapiProperties;
import com.tastyhouse.security.jwt.JwtProperties;

// web-api는 자체 GlobalExceptionHandler(검증 실패 메시지 형식이 "필드명: 메시지"로 다른 등 응답 계약 차이)를
// 쓰므로 공용 api-common-module의 exception 패키지는 스캔하지 않고, 공용 빈 중 FileService만 스캔한다.
// (스캔하면 @RestControllerAdvice 빈이 2개가 된다.)
@SpringBootApplication(scanBasePackages = {"com.tastyhouse.webapi", "com.tastyhouse.apicommon.file", "com.tastyhouse.infrastructure", "com.tastyhouse.external", "com.tastyhouse.security", "com.tastyhouse.logging"})
// @ConfigurationProperties record는 컴포넌트 스캔 대신 명시적으로 등록한다.
// @ConfigurationPropertiesScan은 모듈별 excludeFilters(oauth 제외 등)를 존중하지 않고,
// 앱마다 실제 필요한 프로퍼티 집합이 달라 명시적으로 나열한다.
@EnableConfigurationProperties({
    JwtProperties.class,
    FileStorageProperties.class,
    S3FileStorageProperties.class,
    FirebaseStorageProperties.class,
    SmsProperties.class,
    SolapiProperties.class,
    MailProperties.class,
    TossPaymentProperties.class,
    BbqProperties.class
})
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }
}
