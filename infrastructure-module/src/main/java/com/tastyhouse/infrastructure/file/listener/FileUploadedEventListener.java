package com.tastyhouse.infrastructure.file.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.core.domain.file.domain.event.FileUploadedEvent;

@Slf4j
@Component
public class FileUploadedEventListener {

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
