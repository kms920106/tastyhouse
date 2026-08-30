package com.tastyhouse.infrastructure.file.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.domain.file.event.FileUploadedEvent;

@Component
public class FileUploadedEventListener {

    private static final Logger log = LoggerFactory.getLogger(FileUploadedEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(FileUploadedEvent event) {
        log.info("파일 업로드 완료 — fileId={}, filePath={}, contentType={}, uploadedAt={}",
            event.fileId().value(),
            event.filePath(),
            event.contentType(),
            event.uploadedAt()
        );
    }
}
