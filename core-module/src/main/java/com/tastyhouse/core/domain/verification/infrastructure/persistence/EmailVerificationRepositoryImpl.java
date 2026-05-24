package com.tastyhouse.core.domain.verification.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.verification.domain.model.EmailVerification;
import com.tastyhouse.core.domain.verification.domain.model.EmailVerificationStatus;
import com.tastyhouse.core.domain.verification.domain.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.tastyhouse.core.domain.verification.domain.model.QEmailVerification.emailVerification;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepositoryImpl implements EmailVerificationRepository {

    private final EmailVerificationJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public EmailVerification save(EmailVerification emailVerification) {
        return jpaRepository.save(emailVerification);
    }

    @Override
    public Optional<EmailVerification> findLatestPendingByEmail(String email, EmailVerificationStatus status) {
        EmailVerification result = queryFactory
            .selectFrom(emailVerification)
            .where(
                emailVerification.email.eq(email),
                emailVerification.status.eq(status)
            )
            .orderBy(emailVerification.createdAt.desc())
            .limit(1)
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public void expireAllPendingByEmail(String email) {
        queryFactory
            .update(emailVerification)
            .set(emailVerification.status, EmailVerificationStatus.EXPIRED)
            .where(
                emailVerification.email.eq(email),
                emailVerification.status.eq(EmailVerificationStatus.PENDING)
            )
            .execute();
    }

    @Override
    public void expireAllOverdue(LocalDateTime now) {
        queryFactory
            .update(emailVerification)
            .set(emailVerification.status, EmailVerificationStatus.EXPIRED)
            .where(
                emailVerification.status.eq(EmailVerificationStatus.PENDING),
                emailVerification.expiresAt.lt(now)
            )
            .execute();
    }
}
