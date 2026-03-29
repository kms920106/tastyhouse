package com.tastyhouse.core.repository.verification;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.verification.EmailVerification;
import com.tastyhouse.core.entity.verification.EmailVerificationStatus;
import com.tastyhouse.core.entity.verification.QEmailVerification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepositoryImpl implements EmailVerificationRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<EmailVerification> findLatestPendingByEmail(String email, EmailVerificationStatus status) {
        QEmailVerification ev = QEmailVerification.emailVerification;
        EmailVerification result = queryFactory
            .selectFrom(ev)
            .where(
                ev.email.eq(email),
                ev.status.eq(status)
            )
            .orderBy(ev.createdAt.desc())
            .limit(1)
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public void expireAllPendingByEmail(String email) {
        QEmailVerification ev = QEmailVerification.emailVerification;
        queryFactory
            .update(ev)
            .set(ev.status, EmailVerificationStatus.EXPIRED)
            .where(
                ev.email.eq(email),
                ev.status.eq(EmailVerificationStatus.PENDING)
            )
            .execute();
    }

    @Override
    public void expireAllOverdue(LocalDateTime now) {
        QEmailVerification ev = QEmailVerification.emailVerification;
        queryFactory
            .update(ev)
            .set(ev.status, EmailVerificationStatus.EXPIRED)
            .where(
                ev.status.eq(EmailVerificationStatus.PENDING),
                ev.expiresAt.lt(now)
            )
            .execute();
    }
}
