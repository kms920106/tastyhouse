package com.tastyhouse.infrastructure.mail.listener;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.mail.event.MailVerifiedEvent;
import com.tastyhouse.domain.mail.vo.MailVerificationId;
import com.tastyhouse.infrastructure.shared.listener.ListenerLogCapture;

import static org.assertj.core.api.Assertions.assertThat;

class MailVerificationEventListenerTest {
    private final MailVerificationEventListener listener = new MailVerificationEventListener();

    private ListenerLogCapture logCapture;

    @BeforeEach
    void attachLogCapture() {
        logCapture = ListenerLogCapture.attachTo(MailVerificationEventListener.class);
    }

    @AfterEach
    void detachLogCapture() {
        logCapture.detach();
    }

    @Test
    @DisplayName("메일 인증 완료 이벤트를 받으면 인증 식별자·이메일·완료 시각을 기록한다")
    void logsVerifiedEvent() {
        LocalDateTime verifiedAt = LocalDateTime.of(2026, 4, 4, 14, 20);
        MailVerifiedEvent event = new MailVerifiedEvent(
            MailVerificationId.of(88L),
            "member@example.com",
            verifiedAt
        );

        listener.on(event);

        assertThat(logCapture.singleFormattedMessage())
            .contains("메일 인증 완료")
            .contains("88")
            .contains("member@example.com")
            .contains(verifiedAt.toString());
    }
}
