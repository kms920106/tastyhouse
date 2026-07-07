package com.tastyhouse.core.domain.member.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.member.domain.model.MemberGrade;
import com.tastyhouse.core.domain.member.domain.model.MemberStatus;
import com.tastyhouse.core.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.domain.member.domain.model.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Optional<Member> findById(MemberId memberId) {
        return memberJpaRepository.findById(memberId.value());
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetchOne()
        );
    }

    @Override
    public boolean existsByUsername(String username) {
        return queryFactory
            .selectOne()
            .from(member)
            .where(member.username.eq(username))
            .fetchFirst() != null;
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return queryFactory
            .selectOne()
            .from(member)
            .where(member.nickname.eq(nickname))
            .fetchFirst() != null;
    }

    @Override
    public Optional<Member> findByNickname(String nickname) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(member)
                .where(member.nickname.eq(nickname))
                .fetchOne()
        );
    }

    @Override
    public PageResult<MemberWithProfileImageResult> findByNicknameContaining(String nickname, PageQuery pageQuery) {
        List<MemberWithProfileImageResult> content = queryFactory
            .select(Projections.constructor(MemberWithProfileImageResult.class,
                member.id,
                member.nickname,
                member.memberGrade,
                member.statusMessage,
                uploadedFile.filePath
            ))
            .from(member)
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .where(member.nickname.containsIgnoreCase(nickname))
            .orderBy(member.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Long total = queryFactory
            .select(member.count())
            .from(member)
            .where(member.nickname.containsIgnoreCase(nickname))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public boolean existsByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus) {
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
    public Optional<Member> findByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus) {
        return Optional.ofNullable(
            queryFactory
                .selectFrom(member)
                .where(
                    member.phoneNumber.value.eq(phoneNumber),
                    member.memberStatus.ne(memberStatus)
                )
                .fetchOne()
        );
    }

    @Override
    public long bulkUpdateGrade(List<Long> memberIds, MemberGrade grade) {
        return queryFactory.update(member)
            .set(member.memberGrade, grade)
            .where(member.id.in(memberIds))
            .execute();
    }

    @Override
    public Optional<MemberWithProfileImageResult> findMemberWithProfileImageById(MemberId memberId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(MemberWithProfileImageResult.class,
                    member.id,
                    member.nickname,
                    member.memberGrade,
                    member.statusMessage,
                    uploadedFile.filePath
                ))
                .from(member)
                .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
                .where(member.id.eq(memberId.value()))
                .fetchOne()
        );
    }

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }
}
