package com.tastyhouse.external.crawling;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;

import com.tastyhouse.external.crawling.bbq.BbqProperties;
import com.tastyhouse.external.region.AdminDongBoundaryProperties;

/**
 * infrastructure:crawling 모듈의 auto-configuration — BBQ 메뉴 크롤링 · 행정동 경계 수집 · 원격 이미지 다운로드.
 *
 * <p>전부 배치 작업에서만 쓰므로 batch-module만 이 모듈을 의존하고, 클래스패스 존재만으로
 * 활성화된다. {@link RemoteImageDownloader}는 persistence의 {@code FileDomainConfig}가 등록하는
 * {@code FileUploadService} 빈에 런타임 의존한다.
 */
@AutoConfiguration
@ComponentScan(basePackages = {
    "com.tastyhouse.external.crawling",
    "com.tastyhouse.external.region"
})
@EnableConfigurationProperties({
    BbqProperties.class,
    AdminDongBoundaryProperties.class
})
public class CrawlingModuleAutoConfiguration {
}
