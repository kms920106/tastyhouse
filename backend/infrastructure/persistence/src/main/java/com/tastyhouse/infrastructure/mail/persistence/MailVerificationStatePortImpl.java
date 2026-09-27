package com.tastyhouse.infrastructure.mail.persistence;

import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.mail.port.out.MailVerificationStatusCodes;
import com.tastyhouse.application.mail.port.out.write.MailVerificationState;
import com.tastyhouse.application.mail.port.out.write.MailVerificationStatePort;

import static com.tastyhouse.infrastructure.mail.persistence.QMailVerificationJpaEntity.mailVerificationJpaEntity;

@Repository
public class MailVerificationStatePortImpl implements MailVerificationStatePort {
    private final MailVerificationJpaRepository jpaRepository;
    private final JPAQueryFactory queryFactory;

    public MailVerificationStatePortImpl(MailVerificationJpaRepository jpaRepository, JPAQueryFactory queryFactory) {
        this.jpaRepository = jpaRepository;
        this.queryFactory = queryFactory;
    }

    @Override
    public MailVerificationState save(MailVerificationState state) {
        if (state.id() == null) {
            MailVerificationJpaEntity saved = jpaRepository.save(MailVerificationMapper.toEntity(state));
            return MailVerificationMapper.toState(saved);
        }

        MailVerificationJpaEntity entity = jpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 메일 인증입니다: " + state.id()));
        MailVerificationMapper.applyChanges(entity, state);
        return MailVerificationMapper.toState(entity);
    }

    @Override
    public Optional<MailVerificationState> findLatestPendingByEmail(String email, String status) {
        MailVerificationJpaEntity result = queryFactory
            .selectFrom(mailVerificationJpaEntity)
            .where(
                mailVerificationJpaEntity.email.eq(email),
                mailVerificationJpaEntity.status.eq(status)
            )
            .orderBy(mailVerificationJpaEntity.createdAt.desc())
            .limit(1)
            .fetchOne();
        return Optional.ofNullable(result).map(MailVerificationMapper::toState);
    }

    @Override
    public void expireAllPendingByEmail(String email) {
        queryFactory
            .update(mailVerificationJpaEntity)
            .set(mailVerificationJpaEntity.status, MailVerificationStatusCodes.EXPIRED)
            .where(
                mailVerificationJpaEntity.email.eq(email),
                mailVerificationJpaEntity.status.eq(MailVerificationStatusCodes.PENDING)
            )
            .execute();
    }
}
