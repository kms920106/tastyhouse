package com.tastyhouse.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.tastyhouse.external.crawling.bbq.BbqProperties;
import com.tastyhouse.external.file.FileStorageProperties;
import com.tastyhouse.external.file.firebase.FirebaseStorageProperties;
import com.tastyhouse.external.file.s3.S3FileStorageProperties;
import com.tastyhouse.external.mail.MailProperties;
import com.tastyhouse.external.payment.toss.TossPaymentProperties;
import com.tastyhouse.external.sms.SmsProperties;
import com.tastyhouse.external.sms.solapi.SolapiProperties;

// 배치는 소셜 로그인을 사용하지 않으므로 external 의 OAuth 클라이언트 빈 스캔을 제외한다.
// (스캔에 걸리면 web-api application.yml 에만 있는 apple/kakao/naver/facebook 프로퍼티를
//  해석할 수 없어 기동 시 Could not resolve placeholder 'apple.team-id' 로 실패한다.)
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.tastyhouse.batch", "com.tastyhouse.infrastructure", "com.tastyhouse.external", "com.tastyhouse.logging"})
@ComponentScan(
    basePackages = {"com.tastyhouse.batch", "com.tastyhouse.infrastructure", "com.tastyhouse.external", "com.tastyhouse.logging"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.tastyhouse\\.external\\.oauth\\..*"
    )
)
// @ConfigurationProperties record는 컴포넌트 스캔 대신 명시적으로 등록한다. batch-module은
// com.tastyhouse.security를 스캔하지 않으므로(JwtTokenProvider 등 인증 빈 불필요) JwtProperties는 등록하지 않는다.
@EnableConfigurationProperties({
    FileStorageProperties.class,
    S3FileStorageProperties.class,
    FirebaseStorageProperties.class,
    SmsProperties.class,
    SolapiProperties.class,
    MailProperties.class,
    TossPaymentProperties.class,
    BbqProperties.class
})
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}
