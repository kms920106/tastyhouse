package com.tastyhouse.external.aws;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.tastyhouse.external.aws.s3.S3FileStorageProperties;

/**
 * infrastructure:aws 모듈의 auto-configuration — S3 파일 저장 · SES 메일 발송 · SNS SMS 발송.
 *
 * <p><b>현재 어느 앱도 이 모듈을 의존하지 않는다.</b> 기본 provider가 전부 비-AWS
 * ({@code file.provider=firebase} · {@code mail.provider=javamail} · {@code sms.provider=solapi})라
 * AWS 경로가 활성화되지 않기 때문이며, {@code settings.gradle} 포함으로 컴파일만 검증한다.
 * jar가 클래스패스에 없으므로 이 auto-configuration도 발화하지 않는다.
 * 전환 절차는 이 모듈의 {@code AGENTS.md}를 참조한다.
 */
@AutoConfiguration
@ComponentScan("com.tastyhouse.external.aws")
@EnableConfigurationProperties(S3FileStorageProperties.class)
public class AwsModuleAutoConfiguration {
}
