package com.tastyhouse.application.phoneverification.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.phoneverification.model.SmsVerification;
import com.tastyhouse.domain.phoneverification.model.SmsVerificationStatus;
import com.tastyhouse.application.phoneverification.port.out.SmsSendFailure;
import com.tastyhouse.application.phoneverification.port.out.SmsSendResult;
import com.tastyhouse.application.phoneverification.port.out.SmsSenderPort;
import com.tastyhouse.application.phoneverification.port.out.write.SmsVerificationLoadPort;
import com.tastyhouse.application.phoneverification.port.out.write.SmsVerificationSavePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.WebErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmsVerificationServiceTest {

    @Test
    @DisplayName("issue는 인증코드를 저장하고 그 코드를 담은 SMS를 발송한다")
    void issue_sendsSmsWithGeneratedCode() {
        RecordingSmsSender smsSender = new RecordingSmsSender();
        FakeSmsVerificationPersistence repository = new FakeSmsVerificationPersistence();
        SmsVerificationService service = new SmsVerificationService(repository, repository, smsSender, event -> {
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
        FakeSmsVerificationPersistence repository = new FakeSmsVerificationPersistence();
        SmsVerificationService service = new SmsVerificationService(repository, repository, new RecordingSmsSender(), event -> {
        });

        service.issue("01012345678");

        assertThat(repository.callOrder).containsExactly("expire:01012345678", "save");
    }

    @Test
    @DisplayName("issue는 발송이 실패하면 예외를 전파한다 — 호출자 트랜잭션이 롤백되어 유령 인증코드가 남지 않는다")
    void issue_propagatesSenderFailure() {
        FakeSmsVerificationPersistence repository = new FakeSmsVerificationPersistence();
        SmsSenderPort failingSender = (to, content) -> {
            throw new IllegalStateException("SMS 발송 실패");
        };
        SmsVerificationService service = new SmsVerificationService(repository, repository, failingSender, event -> {
        });

        assertThatThrownBy(() -> service.issue("01012345678"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("SMS 발송 실패");
    }

    @ParameterizedTest
    @CsvSource({
        "NO_RESPONSE, SMS_SEND_NO_RESPONSE",
        "FAILED, SMS_SEND_FAILED",
        "API_ERROR, SMS_SEND_API_ERROR"
    })
    @DisplayName("issue는 발송 실패 종류를 기존과 같은 SMS ErrorCode로 번역한다")
    void issue_translatesFailureKindToErrorCode(SmsSendFailure failure, WebErrorCode expected) {
        SmsSenderPort failingSender = (to, content) -> SmsSendResult.failed(failure);
        FakeSmsVerificationPersistence fakeSmsVerificationPersistence = new FakeSmsVerificationPersistence();
        SmsVerificationService service = new SmsVerificationService(
            fakeSmsVerificationPersistence,
            fakeSmsVerificationPersistence, failingSender, event -> {
        });

        assertThatThrownBy(() -> service.issue("01012345678"))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", expected)
            .hasNoCause();
    }

    @Test
    @DisplayName("issue는 발송 실패 결과의 원인 예외를 번역한 예외에 보존한다")
    void issue_preservesFailureCause() {
        IllegalStateException cause = new IllegalStateException("API 5xx");
        SmsSenderPort failingSender = (to, content) -> SmsSendResult.failed(SmsSendFailure.API_ERROR, cause);
        FakeSmsVerificationPersistence fakeSmsVerificationPersistence = new FakeSmsVerificationPersistence();
        SmsVerificationService service = new SmsVerificationService(
            fakeSmsVerificationPersistence,
            fakeSmsVerificationPersistence, failingSender, event -> {
        });

        assertThatThrownBy(() -> service.issue("01012345678"))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", WebErrorCode.SMS_SEND_API_ERROR)
            .hasCause(cause);
    }

    @Test
    @DisplayName("confirm은 발급된 인증이 없으면 예외를 던진다")
    void confirm_withoutPendingVerification_throws() {
        FakeSmsVerificationPersistence fakeSmsVerificationPersistence = new FakeSmsVerificationPersistence();
        SmsVerificationService service = new SmsVerificationService(
            fakeSmsVerificationPersistence,
            fakeSmsVerificationPersistence, new RecordingSmsSender(), event -> {
        });

        assertThatThrownBy(() -> service.confirm("01012345678", "123456"))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", WebErrorCode.SMS_VERIFICATION_CODE_NOT_FOUND);
    }

    @Test
    @DisplayName("confirm은 검증 성공 시 상태 전이를 저장하고 이벤트를 발행한다")
    void confirm_savesTransitionAndPublishesEvent() {
        FakeSmsVerificationPersistence repository = new FakeSmsVerificationPersistence();
        RecordingSmsSender smsSender = new RecordingSmsSender();
        List<Object> published = new ArrayList<>();
        DomainEventPublisher publisher = published::add;
        SmsVerificationService service = new SmsVerificationService(repository, repository, smsSender, publisher);

        SmsVerification issued = service.issue("01012345678");
        repository.pending = issued;
        service.confirm("01012345678", issued.getVerificationCode().value());

        assertThat(published).hasSize(1);
        assertThat(repository.saved).hasSize(2);
    }

    private static final class RecordingSmsSender implements SmsSenderPort {

        private final List<String> sent = new ArrayList<>();
        private String lastTo;
        private String lastContent;

        @Override
        public SmsSendResult send(String to, String content) {
            this.lastTo = to;
            this.lastContent = content;
            this.sent.add(to);
            return SmsSendResult.sent();
        }

        int sentCount() {
            return sent.size();
        }
    }

    private static final class FakeSmsVerificationPersistence implements SmsVerificationLoadPort, SmsVerificationSavePort {

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
