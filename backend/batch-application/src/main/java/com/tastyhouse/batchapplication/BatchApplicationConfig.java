package com.tastyhouse.batchapplication;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * batch-application 모듈의 진입점 설정.
 *
 * <p>이 모듈을 쓰는 앱은 scanBasePackages 대신 이 클래스를 {@code @Import} 한다
 * (InfrastructureModuleConfig와 동일한 표준 구성).
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan("com.tastyhouse.batchapplication")
public class BatchApplicationConfig {
}
