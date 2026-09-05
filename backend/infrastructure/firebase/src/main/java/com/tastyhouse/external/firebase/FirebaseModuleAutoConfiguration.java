package com.tastyhouse.external.firebase;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

/**
 * infrastructure:firebase 모듈의 auto-configuration — Firebase Storage 파일 저장 전략.
 *
 * <p>클래스패스 존재만으로 활성화된다. 현재 4개 앱 전부 이 모듈을 {@code runtimeOnly}로 의존한다.
 * {@link FirebaseFileStorage}는 {@code @ConditionalOnProperty(file.provider=firebase)}라서
 * 모듈이 클래스패스에 있어도 provider가 다르면 빈이 등록되지 않는다.
 */
@AutoConfiguration
@ComponentScan("com.tastyhouse.external.firebase")
@EnableConfigurationProperties(FirebaseStorageProperties.class)
public class FirebaseModuleAutoConfiguration {
}
