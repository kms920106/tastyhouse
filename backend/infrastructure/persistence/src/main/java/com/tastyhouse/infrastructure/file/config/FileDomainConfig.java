package com.tastyhouse.infrastructure.file.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.file.port.FileStoragePort;
import com.tastyhouse.domain.file.repository.UploadedFileRepository;
import com.tastyhouse.domain.file.service.FileUploadService;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

/**
 * file 컨텍스트의 도메인 서비스(POJO) 빈 등록 설정.
 *
 * <p>도메인 서비스는 {@code @Service} 없는 순수 POJO라 Spring이 스캔할 수 없으므로,
 * 이 컨텍스트에 새 POJO 도메인 서비스를 추가하면 여기에 {@code @Bean}을 추가한다.
 */
@Configuration(proxyBeanMethods = false)
public class FileDomainConfig {

    /**
     * 파일 업로드 규칙 — 규격 검증·스토리지 저장·메타 저장·이벤트 발행을 한 트랜잭션에서 원자로 묶는
     * 액터 무관 연산(web·admin·ceo 업로드와 batch 외부 이미지 다운로드가 공유).
     */
    @Bean
    public FileUploadService fileUploadService(
        UploadedFileRepository uploadedFileRepository,
        FileStoragePort fileStoragePort,
        DomainEventPublisher domainEventPublisher
    ) {
        return new FileUploadService(uploadedFileRepository, fileStoragePort, domainEventPublisher);
    }
}
