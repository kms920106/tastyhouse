package com.tastyhouse.application;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 *  쓰는 application 계층 진입점 설정.
 *
 * <p>이 앱은 scanBasePackages 대신 이 클래스를 {@code @Import} 한다
 * (InfrastructureModuleConfig와 동일한 표준 구성).
 *
 * <p><b>챕터 03 — 패키지가 아니라 마커 애노테이션으로 스캔한다.</b> 4개 앱의 클래스가
 * {@code com.tastyhouse.application} 한 패키지로 평탄화돼 패키지로는 앱을 가릴 수 없다.
 * {@code useDefaultFilters = false}이므로 {@link CeoApp}가 붙은 것만 빈으로 뜨고,
 * <b>마커 없는 {@code @Service}는 어느 앱에도 뜨지 않는다</b> — 그 누락은
 * ArchUnit {@code beansShouldHaveExactlyOneAppMarker}가 잡는다.
 */
@Configuration(proxyBeanMethods = false)
@ComponentScan(
    basePackages = "com.tastyhouse.application",
    useDefaultFilters = false,
    includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = CeoApp.class))
public class CeoApplicationConfig {
}
