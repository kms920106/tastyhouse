package com.tastyhouse.core.repository.member;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.entity.user.MemberGrade;
import com.tastyhouse.core.entity.user.MemberStatus;
import com.tastyhouse.core.entity.user.QMember;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Member> findById(Long memberId) {
        return memberJpaRepository.findById(memberId);
    }

    @Override
    public boolean existsById(Long memberId) {
        return memberJpaRepository.existsById(memberId);
    }

    @Override
    public List<Member> findAllById(Collection<Long> memberIds) {
        QMember member = QMember.member;
        return queryFactory
            .selectFrom(member)
            .where(member.id.in(memberIds))
            .fetch();
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        QMember member = QMember.member;
        Member result = queryFactory
            .selectFrom(member)
            .where(member.username.eq(username))
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsByUsername(String username) {
        QMember member = QMember.member;
        return queryFactory
            .selectOne()
            .from(member)
            .where(member.username.eq(username))
            .fetchFirst() != null;
    }

    @Override
    public boolean existsByNickname(String nickname) {
        QMember member = QMember.member;
        return queryFactory
            .selectOne()
            .from(member)
            .where(member.nickname.eq(nickname))
            .fetchFirst() != null;
    }

    @Override
    public Optional<Member> findByNickname(String nickname) {
        QMember member = QMember.member;
        Member result = queryFactory
            .selectFrom(member)
            .where(member.nickname.eq(nickname))
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Page<Member> findByNicknameContaining(String nickname, Pageable pageable) {
        QMember member = QMember.member;

        List<Member> content = queryFactory
            .selectFrom(member)
            .where(member.nickname.containsIgnoreCase(nickname))
            .orderBy(member.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .select(member.count())
            .from(member)
            .where(member.nickname.containsIgnoreCase(nickname))
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public boolean existsByPhoneNumberValueAndMemberStatusNot(String phoneNumber, MemberStatus memberStatus) {
        QMember member = QMember.member;
        return queryFactory
            .selectOne()
            .from(member)
            .where(
                member.phoneNumber.value.eq(phoneNumber),
                member.memberStatus.ne(memberStatus)
            )
            .fetchFirst() != null;
    }

    @Override
    public Optional<Member> findByPhoneNumberValueAndMemberStatusNot(String phoneNumber, MemberStatus memberStatus) {
        QMember member = QMember.member;
        Member result = queryFactory
            .selectFrom(member)
            .where(
                member.phoneNumber.value.eq(phoneNumber),
                member.memberStatus.ne(memberStatus)
            )
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public long bulkUpdateGrade(List<Long> memberIds, MemberGrade grade) {
        QMember member = QMember.member;
        return queryFactory.update(member)
            .set(member.memberGrade, grade)
            .where(member.id.in(memberIds))
            .execute();
    }

    @Override
    public Page<Member> findAllMembers(Pageable pageable) {
        QMember member = QMember.member;

        List<Member> content = queryFactory
            .selectFrom(member)
            .orderBy(member.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .select(member.count())
            .from(member)
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
