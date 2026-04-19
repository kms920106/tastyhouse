package com.tastyhouse.core.repository.verification;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.verification.EmailVerification;
import com.tastyhouse.core.entity.verification.EmailVerificationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.tastyhouse.core.entity.verification.QEmailVerification.emailVerification;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepositoryImpl implements EmailVerificationRepository {

    private final JPAQueryFactory queryFactory;

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
