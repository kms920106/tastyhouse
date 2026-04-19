package com.tastyhouse.core.repository.member;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.entity.user.MemberGrade;
import com.tastyhouse.core.entity.user.MemberStatus;
import com.tastyhouse.core.entity.user.dto.MemberProfileDetailDto;
import com.tastyhouse.core.entity.user.dto.MemberWithProfileImageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.tastyhouse.core.entity.file.QUploadedFile.uploadedFile;
import static com.tastyhouse.core.entity.user.QMember.member;

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
    public Optional<Member> findByUsername(String username) {
        Member result = queryFactory
            .selectFrom(member)
            .where(member.username.eq(username))
            .fetchOne();
        return Optional.ofNullable(result);
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
        Member result = queryFactory
            .selectFrom(member)
            .where(member.nickname.eq(nickname))
            .fetchOne();
        return Optional.ofNullable(result);
    }

    @Override
    public Page<MemberWithProfileImageDto> findByNicknameContaining(String nickname, Pageable pageable) {

        List<MemberWithProfileImageDto> content = queryFactory
            .select(Projections.constructor(MemberWithProfileImageDto.class,
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
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        JPAQuery<Long> countQuery = queryFactory
            .select(member.count())
            .from(member)
            .where(member.nickname.containsIgnoreCase(nickname));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public boolean existsByPhoneNumberValueAndMemberStatusNot(String phoneNumber, MemberStatus memberStatus) {
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
        return queryFactory.update(member)
            .set(member.memberGrade, grade)
            .where(member.id.in(memberIds))
            .execute();
    }

    @Override
    public Optional<MemberWithProfileImageDto> findMemberWithProfileImageById(Long memberId) {

        MemberWithProfileImageDto result = queryFactory
            .select(Projections.constructor(MemberWithProfileImageDto.class,
                member.id,
                member.nickname,
                member.memberGrade,
                member.statusMessage,
                uploadedFile.filePath
            ))
            .from(member)
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .where(member.id.eq(memberId))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<MemberProfileDetailDto> findMemberProfileDetailById(Long memberId) {

        MemberProfileDetailDto result = queryFactory
            .select(Projections.constructor(MemberProfileDetailDto.class,
                member.id,
                member.nickname,
                member.memberGrade,
                member.statusMessage,
                uploadedFile.filePath,
                member.fullName,
                member.phoneNumber.value,
                member.username
            ))
            .from(member)
            .leftJoin(uploadedFile).on(member.profileImageFileId.eq(uploadedFile.id))
            .where(member.id.eq(memberId))
            .fetchOne();

        return Optional.ofNullable(result);
    }
}
