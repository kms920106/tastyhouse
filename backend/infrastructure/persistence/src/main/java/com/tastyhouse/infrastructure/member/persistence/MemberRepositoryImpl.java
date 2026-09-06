package com.tastyhouse.infrastructure.member.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.vo.MemberId;

import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;

@Repository
public class MemberRepositoryImpl implements MemberRepository {
    private final JPAQueryFactory queryFactory;
    private final MemberJpaRepository memberJpaRepository;

    public MemberRepositoryImpl(JPAQueryFactory queryFactory, MemberJpaRepository memberJpaRepository) {
        this.queryFactory = queryFactory;
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public Optional<Member> findById(MemberId memberId) {
        return memberJpaRepository.findById(memberId.value())
            .map(MemberMapper::toDomain);
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(memberJpaEntity)
                .where(memberJpaEntity.username.eq(username))
                .fetchOne()
        ).map(MemberMapper::toDomain);
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
    public Optional<Member> findByNickname(String nickname) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(memberJpaEntity)
                .where(memberJpaEntity.nickname.eq(nickname))
                .fetchOne()
        ).map(MemberMapper::toDomain);
    }

    @Override
    public boolean existsByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus) {
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
    public Optional<Member> findByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(memberJpaEntity)
                .where(
                    memberJpaEntity.phoneNumber.value.eq(phoneNumber),
                    memberJpaEntity.memberStatus.ne(memberStatus)
                )
                .fetchOne()
        ).map(MemberMapper::toDomain);
    }

    @Override
    public long bulkUpdateGrade(List<Long> memberIds, MemberGrade grade) {
        return queryFactory.update(memberJpaEntity)
            .set(memberJpaEntity.memberGrade, grade)
            .where(memberJpaEntity.id.in(memberIds))
            .execute();
    }

    @Override
    public Member save(Member member) {
        if (member.getId() == null) {
            MemberJpaEntity saved = memberJpaRepository.save(MemberMapper.toEntity(member));
            return MemberMapper.toDomain(saved);
        }

        MemberJpaEntity entity = memberJpaRepository.findById(member.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다: " + member.getId()));
        MemberMapper.applyChanges(entity, member);
        return MemberMapper.toDomain(entity);
    }
}
