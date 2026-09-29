package com.tastyhouse.application.file.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.file.port.out.FileStoragePort;
import com.tastyhouse.application.file.port.out.write.UploadedFilePersistencePort;
import com.tastyhouse.application.file.service.FileUploadService;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class FileServiceConfig {

    @Bean
    public FileUploadService fileUploadService(
        UploadedFilePersistencePort uploadedFilePersistencePort,
        FileStoragePort fileStoragePort,
        DomainEventPublisher domainEventPublisher
    ) {
        return new FileUploadService(uploadedFilePersistencePort, fileStoragePort, domainEventPublisher);
    }
}
