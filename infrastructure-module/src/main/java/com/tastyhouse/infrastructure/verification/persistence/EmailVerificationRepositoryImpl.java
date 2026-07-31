package com.tastyhouse.infrastructure.verification.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.verification.domain.model.EmailVerification;
import com.tastyhouse.domain.verification.domain.model.EmailVerificationStatus;
import com.tastyhouse.domain.verification.domain.repository.EmailVerificationRepository;

import static com.tastyhouse.infrastructure.verification.persistence.QEmailVerificationJpaEntity.emailVerificationJpaEntity;

@Repository
@RequiredArgsConstructor
public class EmailVerificationRepositoryImpl implements EmailVerificationRepository {

    private final EmailVerificationJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    @Override
    public EmailVerification save(EmailVerification emailVerification) {
        if (emailVerification.getId() == null) {
            EmailVerificationJpaEntity saved = jpaRepository.save(EmailVerificationMapper.toEntity(emailVerification));
            return EmailVerificationMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 감사 필드 파손 위험이 있어 쓰지 않는다.
        EmailVerificationJpaEntity entity = jpaRepository.findById(emailVerification.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 이메일 인증입니다: " + emailVerification.getId()));
        EmailVerificationMapper.applyChanges(entity, emailVerification);
        return EmailVerificationMapper.toDomain(entity);
    }

    @Override
    public Optional<EmailVerification> findLatestPendingByEmail(String email, EmailVerificationStatus status) {
        EmailVerificationJpaEntity result = queryFactory
            .selectFrom(emailVerificationJpaEntity)
            .where(
                emailVerificationJpaEntity.email.eq(email),
                emailVerificationJpaEntity.status.eq(status)
            )
            .orderBy(emailVerificationJpaEntity.createdAt.desc())
            .limit(1)
            .fetchOne();
        return Optional.ofNullable(result).map(EmailVerificationMapper::toDomain);
    }

    @Override
    public void expireAllPendingByEmail(String email) {
        queryFactory
            .update(emailVerificationJpaEntity)
            .set(emailVerificationJpaEntity.status, EmailVerificationStatus.EXPIRED)
            .where(
                emailVerificationJpaEntity.email.eq(email),
                emailVerificationJpaEntity.status.eq(EmailVerificationStatus.PENDING)
            )
            .execute();
    }
}
