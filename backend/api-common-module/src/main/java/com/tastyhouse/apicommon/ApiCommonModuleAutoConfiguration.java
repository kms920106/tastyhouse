package com.tastyhouse.apicommon;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tastyhouse.apicommon.exception.GlobalExceptionHandler;

/**
 * api-common-module의 auto-configuration — 공용 예외 핸들러.
 *
 * <p><b>컴포넌트 스캔을 쓰지 않는다.</b> 이 모듈의 나머지 공용 자산({@code ApiResponse}·
 * {@code PageRequest}·{@code ClientIpResolver} 등)은 빈이 아니라 타입이므로 등록할 것이 없고,
 * 앱별로 켜고 꺼야 하는 빈만 조건부 {@code @Bean}으로 등록한다.
 *
 * <p><b>{@code @ConditionalOnMissingBean(annotation = RestControllerAdvice.class)}</b> —
 * web-api는 자체 {@code com.tastyhouse.webapi.exception.GlobalExceptionHandler}를 쓰므로
 * 공용 핸들러가 등록되면 {@code @RestControllerAdvice} 빈이 둘이 된다. 자체 advice가 있으면
 * 물러나고, 없는 앱(admin-api·ceo-api)에서만 등록된다.
 *
 * <p><b>빈 이름을 {@code sharedGlobalExceptionHandler}로 지정하는 이유</b> — web의 자체 핸들러와
 * 단순명이 같아 기본 빈 이름 {@code globalExceptionHandler}가 충돌한다. 조건이 어떤 이유로
 * 우회되더라도 {@code allow-bean-definition-overriding=false}로 기동이 실패해 조용히 덮이지 않는다.
 *
 * <p><b>{@code @ConditionalOnWebApplication(SERVLET)}</b> — batch-module은 이 모듈을 직접
 * 의존하지 않지만 {@code application → security-core → infrastructure:redis → api-common-module}
 * 전이 사슬로 클래스패스에 갖고 있다. batch의 {@code spring.main.web-application-type: none}이
 * 이 조건을 Negative로 만드는 유일한 근거다.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ApiCommonModuleAutoConfiguration {

    @Bean("sharedGlobalExceptionHandler")
    @ConditionalOnMissingBean(annotation = RestControllerAdvice.class)
    public GlobalExceptionHandler sharedGlobalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
