package com.tastyhouse.infrastructure.member.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.application.member.port.out.write.MemberState;
import com.tastyhouse.application.member.port.out.write.MemberStatePort;

import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;

@Repository
public class MemberStatePortImpl implements MemberStatePort {
    private final JPAQueryFactory queryFactory;
    private final MemberJpaRepository memberJpaRepository;

    public MemberStatePortImpl(JPAQueryFactory queryFactory, MemberJpaRepository memberJpaRepository) {
        this.queryFactory = queryFactory;
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public Optional<MemberState> findById(Long memberId) {
        return memberJpaRepository.findById(memberId)
            .map(MemberMapper::toState);
    }

    @Override
    public Optional<MemberState> findByUsername(String username) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(memberJpaEntity)
                .where(memberJpaEntity.username.eq(username))
                .fetchOne()
        ).map(MemberMapper::toState);
    }

    @Override
    public boolean existsByUsername(String username) {
        return queryFactory
            .selectOne()
            .from(memberJpaEntity)
            .where(memberJpaEntity.username.eq(username))
            .fetchFirst() != null;
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return queryFactory
            .selectOne()
            .from(memberJpaEntity)
            .where(memberJpaEntity.nickname.eq(nickname))
            .fetchFirst() != null;
    }

    @Override
    public Optional<MemberState> findByNickname(String nickname) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(memberJpaEntity)
                .where(memberJpaEntity.nickname.eq(nickname))
                .fetchOne()
        ).map(MemberMapper::toState);
    }

    @Override
    public boolean existsByPhoneNumberAndStatusNot(String phoneNumber, String memberStatus) {
        return queryFactory
            .selectOne()
            .from(memberJpaEntity)
            .where(
                memberJpaEntity.phoneNumber.value.eq(phoneNumber),
                memberJpaEntity.memberStatus.ne(memberStatus)
            )
            .fetchFirst() != null;
    }

    @Override
    public Optional<MemberState> findByPhoneNumberAndStatusNot(String phoneNumber, String memberStatus) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(memberJpaEntity)
                .where(
                    memberJpaEntity.phoneNumber.value.eq(phoneNumber),
                    memberJpaEntity.memberStatus.ne(memberStatus)
                )
                .fetchOne()
        ).map(MemberMapper::toState);
    }

    @Override
    public long bulkUpdateGrade(List<Long> memberIds, String grade) {
        return queryFactory.update(memberJpaEntity)
            .set(memberJpaEntity.memberGrade, grade)
            .where(memberJpaEntity.id.in(memberIds))
            .execute();
    }

    @Override
    public MemberState save(MemberState state) {
        if (state.id() == null) {
            MemberJpaEntity saved = memberJpaRepository.save(MemberMapper.toEntity(state));
            return MemberMapper.toState(saved);
        }

        MemberJpaEntity entity = memberJpaRepository.findById(state.id())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다: " + state.id()));
        MemberMapper.applyChanges(entity, state);
        return MemberMapper.toState(entity);
    }
}
