package com.tastyhouse.external.firebase;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * infrastructure:firebase 모듈의 진입점 설정 — Firebase Storage 파일 저장 전략.
 *
 * <p>{@link FirebaseFileStorage}는 {@code @ConditionalOnProperty(file.provider=firebase)}라서
 * import 하더라도 provider가 다르면 빈이 등록되지 않는다. 현재 4개 앱 전부 이 모듈을 쓴다.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan("com.tastyhouse.external.firebase")
@EnableConfigurationProperties(FirebaseStorageProperties.class)
public class FirebaseModuleConfig {
}
