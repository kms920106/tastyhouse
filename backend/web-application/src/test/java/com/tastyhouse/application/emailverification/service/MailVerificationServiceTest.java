package com.tastyhouse.application.emailverification.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.emailverification.model.MailVerification;
import com.tastyhouse.domain.emailverification.model.MailVerificationPurpose;
import com.tastyhouse.domain.emailverification.model.MailVerificationStatus;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.emailverification.port.out.MailSendResult;
import com.tastyhouse.application.emailverification.port.out.MailSenderPort;
import com.tastyhouse.application.emailverification.port.out.write.MailVerificationLoadPort;
import com.tastyhouse.application.emailverification.port.out.write.MailVerificationSavePort;
import com.tastyhouse.application.member.port.out.write.MemberLoadPort;
import com.tastyhouse.application.member.port.out.write.MemberSavePort;
import com.tastyhouse.application.shared.exception.WebErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MailVerificationServiceTest {

    @Test
    @DisplayName("issue는 인증코드를 저장하고 그 코드를 담은 메일을 발송한다")
    void issue_sendsMailWithGeneratedCode() {
        RecordingMailSender mailSender = new RecordingMailSender();
        FakeMailVerificationPersistence repository = new FakeMailVerificationPersistence();
        MailVerificationService service = new MailVerificationService(
            new FakeMemberPersistence(false), repository, repository, mailSender, event -> {
        });

        MailVerification issued = service.issue("user@tastyhouse.com", MailVerificationPurpose.SIGN_UP);

        assertThat(mailSender.sentCount()).isEqualTo(1);
        assertThat(mailSender.lastTo).isEqualTo("user@tastyhouse.com");
        assertThat(mailSender.lastSubject).contains("회원가입");
        assertThat(mailSender.lastContent).contains(issued.getVerificationCode().value());
        assertThat(repository.saved).hasSize(1);
    }

    @Test
    @DisplayName("issue는 목적에 따라 다른 제목·본문으로 발송한다")
    void issue_usesPurposeSpecificMessage() {
        RecordingMailSender mailSender = new RecordingMailSender();
        FakeMailVerificationPersistence fakeMailVerificationPersistence = new FakeMailVerificationPersistence();
        MailVerificationService service = new MailVerificationService(
            new FakeMemberPersistence(false), fakeMailVerificationPersistence, fakeMailVerificationPersistence, mailSender, event -> {
        });

        service.issue("user@tastyhouse.com", MailVerificationPurpose.PASSWORD_RESET);

        assertThat(mailSender.lastSubject).contains("비밀번호 재설정");
        assertThat(mailSender.lastContent).contains("비밀번호 재설정");
    }

    @Test
    @DisplayName("issue는 저장 전에 같은 이메일의 기존 미완료 인증을 먼저 만료시킨다")
    void issue_expiresPreviousPendingBeforeSaving() {
        FakeMailVerificationPersistence repository = new FakeMailVerificationPersistence();
        MailVerificationService service = new MailVerificationService(
            new FakeMemberPersistence(false), repository, repository, new RecordingMailSender(), event -> {
        });

        service.issue("user@tastyhouse.com", MailVerificationPurpose.SIGN_UP);

        assertThat(repository.callOrder).containsExactly("expire:user@tastyhouse.com", "save");
    }

    @Test
    @DisplayName("issue는 발송이 실패하면 예외를 전파한다 — 호출자 트랜잭션이 롤백되어 유령 인증코드가 남지 않는다")
    void issue_propagatesSenderFailure() {
        MailSenderPort failingSender = (to, subject, content) -> {
            throw new IllegalStateException("메일 발송 실패");
        };
        FakeMailVerificationPersistence fakeMailVerificationPersistence = new FakeMailVerificationPersistence();
        MailVerificationService service = new MailVerificationService(
            new FakeMemberPersistence(false), fakeMailVerificationPersistence, fakeMailVerificationPersistence, failingSender, event -> {
        });

        assertThatThrownBy(() -> service.issue("user@tastyhouse.com", MailVerificationPurpose.SIGN_UP))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("메일 발송 실패");
    }

    @Test
    @DisplayName("issue는 발송 실패 결과를 MAIL_SEND_FAILED로 번역하고 원인 예외를 보존한다")
    void issue_translatesFailedResultToMailSendFailed() {
        IllegalStateException cause = new IllegalStateException("SMTP 연결 거부");
        MailSenderPort failingSender = (to, subject, content) -> MailSendResult.failed(cause);
        FakeMailVerificationPersistence fakeMailVerificationPersistence = new FakeMailVerificationPersistence();
        MailVerificationService service = new MailVerificationService(
            new FakeMemberPersistence(false), fakeMailVerificationPersistence, fakeMailVerificationPersistence, failingSender, event -> {
        });

        assertThatThrownBy(() -> service.issue("user@tastyhouse.com", MailVerificationPurpose.SIGN_UP))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", WebErrorCode.MAIL_SEND_FAILED)
            .hasCause(cause);
    }

    @Test
    @DisplayName("issueForSignUp은 이미 가입된 이메일이면 발급을 거부하고 발송하지 않는다")
    void issueForSignUp_rejectsRegisteredEmail() {
        RecordingMailSender mailSender = new RecordingMailSender();
        FakeMailVerificationPersistence fakeMailVerificationPersistence = new FakeMailVerificationPersistence();
        MailVerificationService service = new MailVerificationService(
            new FakeMemberPersistence(true), fakeMailVerificationPersistence, fakeMailVerificationPersistence, mailSender, event -> {
        });

        assertThatThrownBy(() -> service.issueForSignUp("user@tastyhouse.com"))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", WebErrorCode.MEMBER_EMAIL_ALREADY_REGISTERED);
        assertThat(mailSender.sentCount()).isZero();
    }

    @Test
    @DisplayName("confirmForSignUp은 검증 성공 시 상태 전이를 저장하고 이벤트를 발행한다")
    void confirmForSignUp_savesTransitionAndPublishesEvent() {
        FakeMailVerificationPersistence repository = new FakeMailVerificationPersistence();
        List<Object> published = new ArrayList<>();
        MailVerificationService service = new MailVerificationService(
            new FakeMemberPersistence(false), repository, repository, new RecordingMailSender(), published::add);

        MailVerification issued = service.issue("user@tastyhouse.com", MailVerificationPurpose.SIGN_UP);
        repository.pending = issued;
        service.confirmForSignUp("user@tastyhouse.com", issued.getVerificationCode().value());

        assertThat(published).hasSize(1);
        assertThat(repository.saved).hasSize(2);
    }

    @Test
    @DisplayName("confirm은 발급된 인증이 없으면 예외를 던진다")
    void confirm_withoutPendingVerification_throws() {
        FakeMailVerificationPersistence fakeMailVerificationPersistence = new FakeMailVerificationPersistence();
        MailVerificationService service = new MailVerificationService(
            new FakeMemberPersistence(false), fakeMailVerificationPersistence, fakeMailVerificationPersistence, new RecordingMailSender(), event -> {
        });

        assertThatThrownBy(() -> service.confirm("user@tastyhouse.com", "123456"))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", WebErrorCode.MAIL_VERIFICATION_CODE_NOT_FOUND);
    }

    private static final class RecordingMailSender implements MailSenderPort {

        private final List<String> sent = new ArrayList<>();
        private String lastTo;
        private String lastSubject;
        private String lastContent;

        @Override
        public MailSendResult send(String to, String subject, String content) {
            this.lastTo = to;
            this.lastSubject = subject;
            this.lastContent = content;
            this.sent.add(to);
            return MailSendResult.sent();
        }

        int sentCount() {
            return sent.size();
        }
    }

    private static final class FakeMailVerificationPersistence implements MailVerificationLoadPort, MailVerificationSavePort {

        private final List<MailVerification> saved = new ArrayList<>();
        private final List<String> callOrder = new ArrayList<>();
        private MailVerification pending;
        private long sequence = 1L;

        @Override
        public MailVerification save(MailVerification mailVerification) {
            callOrder.add("save");
            saved.add(mailVerification);
            if (mailVerification.getId() != null) {
                return mailVerification;
            }

            return MailVerification.reconstitute(
                sequence++,
                mailVerification.getEmail(),
                mailVerification.getVerificationCode(),
                mailVerification.getStatus(),
                mailVerification.getExpiresAt(),
                mailVerification.getVerifiedAt(),
                mailVerification.getCreatedAt()
            );
        }

        @Override
        public Optional<MailVerification> findLatestPendingByEmail(String email, MailVerificationStatus status) {
            callOrder.add("findLatestPending:" + email);
            return Optional.ofNullable(pending);
        }

        @Override
        public void expireAllPendingByEmail(String email) {
            callOrder.add("expire:" + email);
        }
    }

    private record FakeMemberPersistence(boolean usernameExists) implements MemberLoadPort, MemberSavePort {

        @Override
        public boolean existsByUsername(String username) {
            return usernameExists;
        }

        @Override
        public Optional<Member> findById(MemberId memberId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Member> findByUsername(String username) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByNickname(String nickname) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Member> findByNickname(String nickname) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Member> findByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long bulkUpdateGrade(List<Long> memberIds, MemberGrade grade) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Member save(Member member) {
            throw new UnsupportedOperationException();
        }
    }
}
