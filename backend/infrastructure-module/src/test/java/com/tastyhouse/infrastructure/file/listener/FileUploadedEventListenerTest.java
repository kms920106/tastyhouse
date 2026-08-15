package com.tastyhouse.infrastructure.file.listener;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.event.FileUploadedEvent;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.infrastructure.shared.listener.ListenerLogCapture;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link FileUploadedEventListener}의 현재 동작을 봉인하는 순수 단위 테스트.
 *
 * <p>협력자 없이 기록만 하는 리스너이므로 무엇이 기록되는지를 {@link ListenerLogCapture}로 확인한다.
 * 기록되는 것은 <b>저장 경로</b>이지 표시용 URL이 아니다 — URL 변환은 조회 시점에 query DAO가
 * {@code FileUrlResolver}로 수행하므로, 이 리스너가 경로를 그대로 남기는 것이 정상이다.
 */
class FileUploadedEventListenerTest {

    private final FileUploadedEventListener listener = new FileUploadedEventListener();

    private ListenerLogCapture logCapture;

    @BeforeEach
    void attachLogCapture() {
        logCapture = ListenerLogCapture.attachTo(FileUploadedEventListener.class);
    }

    @AfterEach
    void detachLogCapture() {
        logCapture.detach();
    }

    @Test
    @DisplayName("파일 업로드 이벤트를 받으면 파일 식별자·저장 경로·컨텐트 타입·업로드 시각을 기록한다")
    void logsUploadedEvent() {
        LocalDateTime uploadedAt = LocalDateTime.of(2026, 4, 3, 9, 0);
        FileUploadedEvent event = new FileUploadedEvent(
            UploadedFileId.of(77L),
            "2026/04/sample.png",
            "image/png",
            uploadedAt
        );

        listener.on(event);

        assertThat(logCapture.singleFormattedMessage())
            .contains("파일 업로드 완료")
            .contains("77")
            .contains("2026/04/sample.png")
            .contains("image/png")
            .contains(uploadedAt.toString());
    }
}
