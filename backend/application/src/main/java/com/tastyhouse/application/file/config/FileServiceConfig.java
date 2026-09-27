package com.tastyhouse.application.file.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tastyhouse.application.file.port.out.FileStoragePort;
import com.tastyhouse.application.file.port.out.write.UploadedFileStatePort;
import com.tastyhouse.application.file.service.FileUploadService;
import com.tastyhouse.application.file.store.UploadedFileRepository;
import com.tastyhouse.application.file.store.UploadedFileStore;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.SharedApp;

@Configuration(proxyBeanMethods = false)
@SharedApp
public class FileServiceConfig {

    @Bean
    public UploadedFileRepository uploadedFileRepository(UploadedFileStatePort uploadedFileStatePort) {
        return new UploadedFileStore(uploadedFileStatePort);
    }

    @Bean
    public FileUploadService fileUploadService(
        UploadedFileRepository uploadedFileRepository,
        FileStoragePort fileStoragePort,
        DomainEventPublisher domainEventPublisher
    ) {
        return new FileUploadService(uploadedFileRepository, fileStoragePort, domainEventPublisher);
    }
}
