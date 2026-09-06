package com.tastyhouse.apicommon;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.tastyhouse.apicommon.exception.GlobalExceptionHandler;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ApiCommonModuleAutoConfiguration {

    @Bean("sharedGlobalExceptionHandler")
    @ConditionalOnMissingBean(annotation = RestControllerAdvice.class)
    public GlobalExceptionHandler sharedGlobalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
