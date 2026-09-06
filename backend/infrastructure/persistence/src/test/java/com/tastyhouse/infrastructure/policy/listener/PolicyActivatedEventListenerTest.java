package com.tastyhouse.infrastructure.policy.listener;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.policy.event.PolicyActivatedEvent;
import com.tastyhouse.domain.policy.model.PolicyType;
import com.tastyhouse.domain.policy.vo.PolicyDocumentId;
import com.tastyhouse.infrastructure.shared.listener.ListenerLogCapture;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyActivatedEventListenerTest {
    private final PolicyActivatedEventListener listener = new PolicyActivatedEventListener();

    private ListenerLogCapture logCapture;

    @BeforeEach
    void attachLogCapture() {
        logCapture = ListenerLogCapture.attachTo(PolicyActivatedEventListener.class);
    }

    @AfterEach
    void detachLogCapture() {
        logCapture.detach();
    }

    @Test
    @DisplayName("정책 활성화 이벤트를 받으면 문서 식별자·유형·버전·전이 시각을 기록한다")
    void logsActivatedEvent() {
        LocalDateTime activatedAt = LocalDateTime.of(2026, 4, 17, 10, 0);
        PolicyActivatedEvent event = new PolicyActivatedEvent(
            PolicyDocumentId.of(801L),
            PolicyType.TERMS_OF_SERVICE,
            "v2.1",
            activatedAt
        );

        listener.on(event);

        assertThat(logCapture.singleFormattedMessage())
            .contains("정책 현행 전이 완료")
            .contains("801")
            .contains(PolicyType.TERMS_OF_SERVICE.name())
            .contains("v2.1")
            .contains(activatedAt.toString());
    }
}
