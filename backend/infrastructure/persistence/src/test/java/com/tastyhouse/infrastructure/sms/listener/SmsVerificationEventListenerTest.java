package com.tastyhouse.infrastructure.sms.listener;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.sms.event.SmsVerifiedEvent;
import com.tastyhouse.domain.sms.vo.SmsVerificationId;
import com.tastyhouse.infrastructure.shared.listener.ListenerLogCapture;

import static org.assertj.core.api.Assertions.assertThat;

class SmsVerificationEventListenerTest {
    private final SmsVerificationEventListener listener = new SmsVerificationEventListener();

    private ListenerLogCapture logCapture;

    @BeforeEach
    void attachLogCapture() {
        logCapture = ListenerLogCapture.attachTo(SmsVerificationEventListener.class);
    }

    @AfterEach
    void detachLogCapture() {
        logCapture.detach();
    }

    @Test
    @DisplayName("SMS 인증 완료 이벤트를 받으면 인증 식별자·휴대폰 번호·완료 시각을 기록한다")
    void logsVerifiedEvent() {
        LocalDateTime verifiedAt = LocalDateTime.of(2026, 4, 5, 11, 45);
        SmsVerifiedEvent event = new SmsVerifiedEvent(
            SmsVerificationId.of(99L),
            "01012345678",
            verifiedAt
        );

        listener.on(event);

        assertThat(logCapture.singleFormattedMessage())
            .contains("SMS 인증 완료")
            .contains("99")
            .contains("01012345678")
            .contains(verifiedAt.toString());
    }
}
