package com.tastyhouse.domain.mail.repository;

import java.util.Optional;

import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationStatus;

/**
 * 메일 인증 write 포트.
 *
 * <p>여기 남은 조회는 모두 상태 전이의 전제 조건이다 — {@code findLatestPendingByEmail}은
 * 코드 대조·만료 판정에, {@code expireAllPendingByEmail}은 "같은 이메일의 기존 미완료 인증을
 * 모두 만료시킨다"는 발급 불변식에 필요하다. 표현 목적 조회는 이 포트에 두지 않는다.
 */
public interface MailVerificationRepository {

    MailVerification save(MailVerification mailVerification);

    Optional<MailVerification> findLatestPendingByEmail(String email, MailVerificationStatus status);

    void expireAllPendingByEmail(String email);
}
