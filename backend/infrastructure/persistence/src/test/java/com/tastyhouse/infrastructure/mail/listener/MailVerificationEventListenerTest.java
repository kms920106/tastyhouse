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

/**
 * {@link MailVerificationEventListener}의 현재 동작을 봉인하는 순수 단위 테스트.
 *
 * <p><b>이 리스너가 메일을 발송하지 않는 것이 정상</b>이라는 점도 함께 고정한다 — 이 이벤트는 인증
 * <b>완료</b> 시점이고 발송은 <b>발급</b> 시점에 필요하므로, 발송은 도메인 서비스
 * {@code MailVerificationService#issue}가 발급과 원자적으로 수행한다. 협력자를 주입받지 않는
 * 생성자 자체가 그 사실의 증거이며, 여기에 Sender가 추가되면 이 테스트가 컴파일되지 않는다.
 */
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
