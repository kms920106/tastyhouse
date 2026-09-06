package com.tastyhouse.infrastructure.file.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.domain.file.port.FileStoragePort;
import com.tastyhouse.domain.file.repository.UploadedFileRepository;
import com.tastyhouse.domain.file.service.FileUploadService;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

@Configuration(proxyBeanMethods = false)
public class FileDomainConfig {
    @Bean
    public FileUploadService fileUploadService(
        UploadedFileRepository uploadedFileRepository,
        FileStoragePort fileStoragePort,
        DomainEventPublisher domainEventPublisher
    ) {
        return new FileUploadService(uploadedFileRepository, fileStoragePort, domainEventPublisher);
    }
}
