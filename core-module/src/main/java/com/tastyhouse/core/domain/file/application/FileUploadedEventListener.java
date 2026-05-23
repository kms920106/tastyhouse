package com.tastyhouse.core.domain.file.application;

import com.tastyhouse.core.domain.file.domain.event.FileUploadedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class FileUploadedEventListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(FileUploadedEvent event) {
        log.info("파일 업로드 완료 — fileId={}, filePath={}, contentType={}, uploadedAt={}",
            event.fileId().value(),
            event.filePath(),
            event.contentType(),
            event.uploadedAt()
        );
    }
}
