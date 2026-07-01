package com.tastyhouse.core.domain.verification.infrastructure.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.verification.domain.model.EmailVerification;
import com.tastyhouse.core.domain.verification.domain.model.EmailVerificationStatus;
import com.tastyhouse.core.domain.verification.domain.repository.EmailVerificationRepository;

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
}
