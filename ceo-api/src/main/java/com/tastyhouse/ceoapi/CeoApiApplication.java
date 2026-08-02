package com.tastyhouse.ceoapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

import com.tastyhouse.external.crawling.bbq.BbqProperties;
import com.tastyhouse.external.file.FileStorageProperties;
import com.tastyhouse.external.file.firebase.FirebaseStorageProperties;
import com.tastyhouse.external.file.s3.S3FileStorageProperties;
import com.tastyhouse.external.mail.MailProperties;
import com.tastyhouse.external.payment.toss.TossPaymentProperties;
import com.tastyhouse.external.sms.SmsProperties;
import com.tastyhouse.external.sms.solapi.SolapiProperties;
import com.tastyhouse.security.jwt.JwtProperties;
import com.tastyhouse.ceoapi.config.CeoSeedProperties;

// 점주 API는 소셜 로그인을 사용하지 않으므로 external 의 OAuth 클라이언트 빈 스캔을 제외한다.
@SpringBootApplication(scanBasePackages = {"com.tastyhouse.ceoapi", "com.tastyhouse.apicommon", "com.tastyhouse.infrastructure", "com.tastyhouse.external", "com.tastyhouse.security", "com.tastyhouse.logging"})
@ComponentScan(
    basePackages = {"com.tastyhouse.ceoapi", "com.tastyhouse.apicommon", "com.tastyhouse.infrastructure", "com.tastyhouse.external", "com.tastyhouse.security", "com.tastyhouse.logging"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.tastyhouse\\.external\\.oauth\\..*"
    )
)
// @ConfigurationProperties record는 컴포넌트 스캔 대신 명시적으로 등록한다.
// @ConfigurationPropertiesScan은 모듈별 excludeFilters(oauth 제외 등)를 존중하지 않아 명시적으로 나열한다.
@EnableConfigurationProperties({
    JwtProperties.class,
    CeoSeedProperties.class,
    FileStorageProperties.class,
    S3FileStorageProperties.class,
    FirebaseStorageProperties.class,
    SmsProperties.class,
    SolapiProperties.class,
    MailProperties.class,
    TossPaymentProperties.class,
    BbqProperties.class
})
public class CeoApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CeoApiApplication.class, args);
    }
}
