package com.tastyhouse.external.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

import com.tastyhouse.external.crawling.bbq.BbqProperties;
import com.tastyhouse.external.file.FileStorageProperties;
import com.tastyhouse.external.file.firebase.FirebaseStorageProperties;
import com.tastyhouse.external.file.s3.S3FileStorageProperties;
import com.tastyhouse.external.mail.MailProperties;
import com.tastyhouse.external.payment.toss.TossPaymentProperties;
import com.tastyhouse.external.region.AdminDongBoundaryProperties;
import com.tastyhouse.external.sms.SmsProperties;
import com.tastyhouse.external.sms.solapi.SolapiProperties;

/**
 * external-api 모듈의 진입점 설정 — OAuth를 제외한 외부 연동 전체.
 *
 * <p>OAuth 클라이언트는 소셜 로그인을 쓰는 앱(web-api)만 필요하므로 이 스캔에서 제외하고
 * {@link com.tastyhouse.external.oauth.ExternalOAuthConfig}로 분리했다. 이전에는 admin/ceo/batch
 * 세 앱이 각자 동일한 REGEX excludeFilters를 복사해 갖고 있었으나, 이제 제외 규칙은 이 한 곳에만
 * 존재하고 앱별 차이는 {@code ExternalOAuthConfig}의 import 유무로 표현된다.
 *
 * <p>OAuth 빈을 스캔하면 web-api의 {@code application.yml}에만 존재하는
 * apple/kakao/naver/facebook 프로퍼티를 해석할 수 없어 기동 시
 * {@code Could not resolve placeholder 'apple.team-id'}로 실패한다(batch-module 실패 이력).
 *
 * <p>{@code @ConfigurationProperties} record는 컴포넌트 스캔 대신 여기서 명시적으로 등록한다.
 * {@code @ConfigurationPropertiesScan}은 모듈별 excludeFilters(OAuth 제외 등)를 존중하지 않으므로
 * 쓰지 않는다.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(
    basePackages = "com.tastyhouse.external",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.tastyhouse\\.external\\.oauth\\..*"
    )
)
@EnableConfigurationProperties({
    FileStorageProperties.class,
    S3FileStorageProperties.class,
    FirebaseStorageProperties.class,
    SmsProperties.class,
    SolapiProperties.class,
    MailProperties.class,
    TossPaymentProperties.class,
    BbqProperties.class,
    AdminDongBoundaryProperties.class
})
public class ExternalApiConfig {
}
