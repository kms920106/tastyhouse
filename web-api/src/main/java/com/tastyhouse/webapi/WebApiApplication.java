package com.tastyhouse.webapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// web-api는 자체 GlobalExceptionHandler(ExternalApiException·NoHandlerFoundException 등 web 전용 핸들러 보유)를
// 쓰므로 공용 api-common-module의 exception 패키지는 스캔하지 않고, 공용 빈 중 FileService만 스캔한다.
@SpringBootApplication(scanBasePackages = {"com.tastyhouse.webapi", "com.tastyhouse.apicommon.file", "com.tastyhouse.infrastructure", "com.tastyhouse.external", "com.tastyhouse.security", "com.tastyhouse.logging"})
public class WebApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApiApplication.class, args);
    }
}
