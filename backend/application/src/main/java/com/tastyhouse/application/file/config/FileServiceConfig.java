package com.tastyhouse.application.file.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.file.port.out.FileStoragePort;
import com.tastyhouse.application.file.service.FileUploadService;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.domain.file.repository.UploadedFileRepository;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class FileServiceConfig {

    @Bean
    public FileUploadService fileUploadService(
        UploadedFileRepository uploadedFileRepository,
        FileStoragePort fileStoragePort,
        DomainEventPublisher domainEventPublisher
    ) {
        return new FileUploadService(uploadedFileRepository, fileStoragePort, domainEventPublisher);
    }
}
