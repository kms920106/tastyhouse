package com.tastyhouse.infrastructure.member.listener;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.event.MemberRegisteredEvent;
import com.tastyhouse.domain.member.event.MemberWithdrawnEvent;
import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.infrastructure.shared.listener.ListenerLogCapture;

import static org.assertj.core.api.Assertions.assertThat;

class MemberEventListenerTest {
    private final MemberEventListener listener = new MemberEventListener();

    private ListenerLogCapture logCapture;

    @BeforeEach
    void attachLogCapture() {
        logCapture = ListenerLogCapture.attachTo(MemberEventListener.class);
    }

    @AfterEach
    void detachLogCapture() {
        logCapture.detach();
    }

    @Test
    @DisplayName("회원가입 이벤트를 받으면 회원 식별자·아이디·가입 시각을 기록한다")
    void logsRegisteredEvent() {
        LocalDateTime registeredAt = LocalDateTime.of(2026, 4, 6, 8, 15);
        MemberRegisteredEvent event = new MemberRegisteredEvent(
            MemberId.of(101L),
            "tasty_member",
            registeredAt
        );

        listener.on(event);

        assertThat(logCapture.singleFormattedMessage())
            .contains("회원가입 완료")
            .contains("101")
            .contains("tasty_member")
            .contains(registeredAt.toString());
    }

    @Test
    @DisplayName("회원탈퇴 이벤트를 받으면 회원 식별자·탈퇴 사유·탈퇴 시각을 기록한다")
    void logsWithdrawnEvent() {
        LocalDateTime withdrawnAt = LocalDateTime.of(2026, 4, 7, 17, 40);
        MemberWithdrawnEvent event = new MemberWithdrawnEvent(
            MemberId.of(102L),
            MemberWithdrawalReason.LOW_USAGE_FREQUENCY,
            withdrawnAt
        );

        listener.on(event);

        assertThat(logCapture.singleFormattedMessage())
            .contains("회원탈퇴 완료")
            .contains("102")
            .contains(MemberWithdrawalReason.LOW_USAGE_FREQUENCY.name())
            .contains(withdrawnAt.toString());
    }
}
