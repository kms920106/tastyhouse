package com.tastyhouse.domain.sms.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;
import com.tastyhouse.domain.sms.port.SmsSender;
import com.tastyhouse.domain.sms.repository.SmsVerificationRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SMS 인증 도메인 서비스 단위 테스트.
 *
 * <p><b>이 테스트의 존재 이유</b>: 과거 발송 책임이 호출부에 흩어져 있어 인증코드 발송 API가 코드를
 * 저장만 하고 실제로 발송하지 않는 버그가 있었다({@code SmsSender}는 프로덕션에서 호출조차 되지
 * 않는 죽은 포트였다). {@code issue}가 발송까지 수행하는지를 여기서 검증하므로 그 회귀가 CI에서
 * 잡힌다. Spring 컨텍스트 없이 fake 포트만으로 검증된다(도메인 프레임워크-프리의 이점).
 */
class SmsVerificationServiceTest {

    @Test
    @DisplayName("issue는 인증코드를 저장하고 그 코드를 담은 SMS를 발송한다")
    void issue_sendsSmsWithGeneratedCode() {
        RecordingSmsSender smsSender = new RecordingSmsSender();
        FakeSmsVerificationRepository repository = new FakeSmsVerificationRepository();
        SmsVerificationService service = new SmsVerificationService(repository, smsSender, event -> {
        });

        SmsVerification issued = service.issue("01012345678");

        assertThat(smsSender.sentCount()).isEqualTo(1);
        assertThat(smsSender.lastTo).isEqualTo("01012345678");
        assertThat(smsSender.lastContent).contains(issued.getVerificationCode().value());
        assertThat(repository.saved).hasSize(1);
    }

    @Test
    @DisplayName("issue는 저장 전에 같은 번호의 기존 미완료 인증을 먼저 만료시킨다")
    void issue_expiresPreviousPendingBeforeSaving() {
        FakeSmsVerificationRepository repository = new FakeSmsVerificationRepository();
        SmsVerificationService service = new SmsVerificationService(repository, new RecordingSmsSender(), event -> {
        });

        service.issue("01012345678");

        assertThat(repository.callOrder).containsExactly("expire:01012345678", "save");
    }

    @Test
    @DisplayName("issue는 발송이 실패하면 예외를 전파한다 — 호출자 트랜잭션이 롤백되어 유령 인증코드가 남지 않는다")
    void issue_propagatesSenderFailure() {
        FakeSmsVerificationRepository repository = new FakeSmsVerificationRepository();
        SmsSender failingSender = (to, content) -> {
            throw new IllegalStateException("SMS 발송 실패");
        };
        SmsVerificationService service = new SmsVerificationService(repository, failingSender, event -> {
        });

        assertThatThrownBy(() -> service.issue("01012345678"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("SMS 발송 실패");
    }

    @Test
    @DisplayName("confirm은 발급된 인증이 없으면 예외를 던진다")
    void confirm_withoutPendingVerification_throws() {
        SmsVerificationService service = new SmsVerificationService(
            new FakeSmsVerificationRepository(), new RecordingSmsSender(), event -> {
        });

        assertThatThrownBy(() -> service.confirm("01012345678", "123456"))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.SMS_VERIFICATION_CODE_NOT_FOUND);
    }

    @Test
    @DisplayName("confirm은 검증 성공 시 상태 전이를 저장하고 이벤트를 발행한다")
    void confirm_savesTransitionAndPublishesEvent() {
        FakeSmsVerificationRepository repository = new FakeSmsVerificationRepository();
        RecordingSmsSender smsSender = new RecordingSmsSender();
        List<Object> published = new ArrayList<>();
        DomainEventPublisher publisher = published::add;
        SmsVerificationService service = new SmsVerificationService(repository, smsSender, publisher);

        SmsVerification issued = service.issue("01012345678");
        repository.pending = issued;
        service.confirm("01012345678", issued.getVerificationCode().value());

        assertThat(published).hasSize(1);
        assertThat(repository.saved).hasSize(2); // issue 1건 + confirm 1건
    }

    private static final class RecordingSmsSender implements SmsSender {

        private final List<String> sent = new ArrayList<>();
        private String lastTo;
        private String lastContent;

        @Override
        public void send(String to, String content) {
            this.lastTo = to;
            this.lastContent = content;
            this.sent.add(to);
        }

        int sentCount() {
            return sent.size();
        }
    }

    private static final class FakeSmsVerificationRepository implements SmsVerificationRepository {

        private final List<SmsVerification> saved = new ArrayList<>();
        private final List<String> callOrder = new ArrayList<>();
        private SmsVerification pending;
        private long sequence = 1L;

        @Override
        public SmsVerification save(SmsVerification smsVerification) {
            callOrder.add("save");
            saved.add(smsVerification);
            if (smsVerification.getId() != null) {
                return smsVerification;
            }
            // 저장 시 식별자가 부여되는 것을 모사한다.
            return SmsVerification.reconstitute(
                sequence++,
                smsVerification.getPhoneNumber(),
                smsVerification.getVerificationCode(),
                smsVerification.getStatus(),
                smsVerification.getExpiresAt(),
                smsVerification.getVerifiedAt(),
                smsVerification.getCreatedAt()
            );
        }

        @Override
        public Optional<SmsVerification> findLatestPendingByPhoneNumber(String phoneNumber, SmsVerificationStatus status) {
            callOrder.add("findLatestPending:" + phoneNumber);
            return Optional.ofNullable(pending);
        }

        @Override
        public void expireAllPendingByPhoneNumber(String phoneNumber) {
            callOrder.add("expire:" + phoneNumber);
        }
    }
}
