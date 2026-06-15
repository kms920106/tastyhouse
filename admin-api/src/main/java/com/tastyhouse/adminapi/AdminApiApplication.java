package com.tastyhouse.adminapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

// 관리자 API는 소셜 로그인을 사용하지 않으므로 external 의 OAuth 클라이언트 빈 스캔을 제외한다.
@SpringBootApplication(scanBasePackages = {"com.tastyhouse.adminapi", "com.tastyhouse.core", "com.tastyhouse.external"})
@ComponentScan(
    basePackages = {"com.tastyhouse.adminapi", "com.tastyhouse.core", "com.tastyhouse.external"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.tastyhouse\\.external\\.oauth\\..*"
    )
)
public class AdminApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApiApplication.class, args);
    }
}
