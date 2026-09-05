package com.tastyhouse.external.payment;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.tastyhouse.external.payment.toss.TossPaymentProperties;

/**
 * infrastructure:payment 모듈의 auto-configuration — 토스페이먼츠 PG 연동.
 *
 * <p>결제는 사용자 앱에서만 일어나므로 web-api만 이 모듈을 의존한다. 클래스패스 존재만으로
 * 활성화되므로, 다른 앱에 의존을 추가하면 그 앱에도 PG 빈이 올라온다.
 */
@AutoConfiguration
@ComponentScan("com.tastyhouse.external.payment")
@EnableConfigurationProperties(TossPaymentProperties.class)
public class PaymentModuleAutoConfiguration {
}
