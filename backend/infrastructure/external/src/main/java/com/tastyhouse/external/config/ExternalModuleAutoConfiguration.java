package com.tastyhouse.external.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.tastyhouse.external.file.FileStorageProperties;

/**
 * infrastructure:external 코어 모듈의 auto-configuration — 벤더 중립 공용 자산만 담는다.
 *
 * <p>클래스패스 존재만으로 활성화된다. 앱은 {@code build.gradle}에서 {@code runtimeOnly}로만
 * 의존하며 이 클래스를 {@code @Import} 하지 않는다.
 *
 * <p>스캔 범위는 {@code WebClient} 빌더({@code external.config})와 파일 저장 SPI·포트 어댑터
 * ({@code external.file}) 두 패키지다. 실제 저장소 구현(Firebase·S3)·OAuth·결제·메시징·크롤링은
 * 각각 별도 모듈로 분리됐고, 앱은 실제로 쓰는 모듈만 의존한다 — 그 모듈들도 각자
 * auto-configuration({@code FirebaseModuleAutoConfiguration}·{@code AwsModuleAutoConfiguration}·
 * {@code OAuthModuleAutoConfiguration}·{@code PaymentModuleAutoConfiguration}·
 * {@code MessagingModuleAutoConfiguration}·{@code CrawlingModuleAutoConfiguration})으로 자기 등록한다.
 *
 * <p>{@code external.file} 하위에 벤더 패키지를 두지 않는 이유가 이 스캔이다 — 하위 패키지가
 * 클래스패스에 있으면 동반 스캔되므로, Firebase는 {@code external.firebase}, S3는
 * {@code external.aws.s3}로 옮겼다. 분리 전에는 OAuth를 REGEX {@code excludeFilters}로 제외했으나,
 * 이제 모듈 경계가 그 역할을 대신하므로 제외 규칙이 필요 없다.
 *
 * <p>{@code @ConfigurationProperties} record는 컴포넌트 스캔 대신 여기서 명시적으로 등록한다
 * ({@code @ConfigurationPropertiesScan} 미사용 방침).
 */
@AutoConfiguration
@ComponentScan(basePackages = {
    "com.tastyhouse.external.config",
    "com.tastyhouse.external.file"
})
@EnableConfigurationProperties(FileStorageProperties.class)
public class ExternalModuleAutoConfiguration {
}
