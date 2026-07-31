package com.tastyhouse.batch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

// 배치는 소셜 로그인을 사용하지 않으므로 external 의 OAuth 클라이언트 빈 스캔을 제외한다.
// (스캔에 걸리면 web-api application.yml 에만 있는 apple/kakao/naver/facebook 프로퍼티를
//  해석할 수 없어 기동 시 Could not resolve placeholder 'apple.team-id' 로 실패한다.)
@EnableScheduling
@SpringBootApplication(scanBasePackages = {"com.tastyhouse.batch", "com.tastyhouse.infrastructure", "com.tastyhouse.external", "com.tastyhouse.logging"})
@ComponentScan(
    basePackages = {"com.tastyhouse.batch", "com.tastyhouse.infrastructure", "com.tastyhouse.external", "com.tastyhouse.logging"},
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.REGEX,
        pattern = "com\\.tastyhouse\\.external\\.oauth\\..*"
    )
)
public class BatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(BatchApplication.class, args);
    }
}
