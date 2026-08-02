package com.tastyhouse.domain.mail.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.mail.service.MailVerificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationPurpose;
import com.tastyhouse.domain.mail.model.MailVerificationStatus;
import com.tastyhouse.domain.mail.port.MailSender;
import com.tastyhouse.domain.mail.repository.MailVerificationRepository;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 메일 인증 도메인 서비스 단위 테스트.
 *
 * <p><b>이 테스트의 존재 이유</b>: 과거 발송 책임이 호출부에 흩어져 있어 회원가입 인증코드 발송
 * API가 코드를 저장만 하고 실제로 발송하지 않는 버그가 있었다(비밀번호 재설정 파사드만 발송을
 * 직접 호출). {@code issue}가 발송까지 수행하는지를 여기서 검증하므로 그 회귀가 CI에서 잡힌다.
 * Spring 컨텍스트 없이 fake 포트만으로 검증된다(도메인 프레임워크-프리의 이점).
 */
class MailVerificationServiceTest {

    @Test
    @DisplayName("issue는 인증코드를 저장하고 그 코드를 담은 메일을 발송한다")
    void issue_sendsMailWithGeneratedCode() {
        RecordingMailSender mailSender = new RecordingMailSender();
        FakeMailVerificationRepository repository = new FakeMailVerificationRepository();
        MailVerificationService service = new MailVerificationService(
            new FakeMemberRepository(false), repository, mailSender, event -> {
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
        MailVerificationService service = new MailVerificationService(
            new FakeMemberRepository(false), new FakeMailVerificationRepository(), mailSender, event -> {
        });

        service.issue("user@tastyhouse.com", MailVerificationPurpose.PASSWORD_RESET);

        assertThat(mailSender.lastSubject).contains("비밀번호 재설정");
        assertThat(mailSender.lastContent).contains("비밀번호 재설정");
    }

    @Test
    @DisplayName("issue는 저장 전에 같은 이메일의 기존 미완료 인증을 먼저 만료시킨다")
    void issue_expiresPreviousPendingBeforeSaving() {
        FakeMailVerificationRepository repository = new FakeMailVerificationRepository();
        MailVerificationService service = new MailVerificationService(
            new FakeMemberRepository(false), repository, new RecordingMailSender(), event -> {
        });

        service.issue("user@tastyhouse.com", MailVerificationPurpose.SIGN_UP);

        assertThat(repository.callOrder).containsExactly("expire:user@tastyhouse.com", "save");
    }

    @Test
    @DisplayName("issue는 발송이 실패하면 예외를 전파한다 — 호출자 트랜잭션이 롤백되어 유령 인증코드가 남지 않는다")
    void issue_propagatesSenderFailure() {
        MailSender failingSender = (to, subject, content) -> {
            throw new IllegalStateException("메일 발송 실패");
        };
        MailVerificationService service = new MailVerificationService(
            new FakeMemberRepository(false), new FakeMailVerificationRepository(), failingSender, event -> {
        });

        assertThatThrownBy(() -> service.issue("user@tastyhouse.com", MailVerificationPurpose.SIGN_UP))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("메일 발송 실패");
    }

    @Test
    @DisplayName("issueForSignUp은 이미 가입된 이메일이면 발급을 거부하고 발송하지 않는다")
    void issueForSignUp_rejectsRegisteredEmail() {
        RecordingMailSender mailSender = new RecordingMailSender();
        MailVerificationService service = new MailVerificationService(
            new FakeMemberRepository(true), new FakeMailVerificationRepository(), mailSender, event -> {
        });

        assertThatThrownBy(() -> service.issueForSignUp("user@tastyhouse.com"))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_EMAIL_ALREADY_REGISTERED);
        assertThat(mailSender.sentCount()).isZero();
    }

    @Test
    @DisplayName("confirmForSignUp은 검증 성공 시 상태 전이를 저장하고 이벤트를 발행한다")
    void confirmForSignUp_savesTransitionAndPublishesEvent() {
        FakeMailVerificationRepository repository = new FakeMailVerificationRepository();
        List<Object> published = new ArrayList<>();
        MailVerificationService service = new MailVerificationService(
            new FakeMemberRepository(false), repository, new RecordingMailSender(), published::add);

        MailVerification issued = service.issue("user@tastyhouse.com", MailVerificationPurpose.SIGN_UP);
        repository.pending = issued;
        service.confirmForSignUp("user@tastyhouse.com", issued.getVerificationCode().value());

        assertThat(published).hasSize(1);
        assertThat(repository.saved).hasSize(2); // issue 1건 + confirm 1건
    }

    @Test
    @DisplayName("confirm은 발급된 인증이 없으면 예외를 던진다")
    void confirm_withoutPendingVerification_throws() {
        MailVerificationService service = new MailVerificationService(
            new FakeMemberRepository(false), new FakeMailVerificationRepository(), new RecordingMailSender(), event -> {
        });

        assertThatThrownBy(() -> service.confirm("user@tastyhouse.com", "123456"))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MAIL_VERIFICATION_CODE_NOT_FOUND);
    }

    private static final class RecordingMailSender implements MailSender {

        private final List<String> sent = new ArrayList<>();
        private String lastTo;
        private String lastSubject;
        private String lastContent;

        @Override
        public void send(String to, String subject, String content) {
            this.lastTo = to;
            this.lastSubject = subject;
            this.lastContent = content;
            this.sent.add(to);
        }

        int sentCount() {
            return sent.size();
        }
    }

    private static final class FakeMailVerificationRepository implements MailVerificationRepository {

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
            // 저장 시 식별자가 부여되는 것을 모사한다.
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

    /**
     * {@code existsByUsername}만 사용하는 도메인 서비스에 맞춘 최소 fake. 나머지 메서드는 호출되지 않는다.
     */
    private record FakeMemberRepository(boolean usernameExists) implements MemberRepository {

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
