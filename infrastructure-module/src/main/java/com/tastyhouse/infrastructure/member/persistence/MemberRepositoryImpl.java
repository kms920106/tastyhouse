package com.tastyhouse.infrastructure.member.persistence;

import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.member.domain.model.MemberGrade;
import com.tastyhouse.core.domain.member.domain.model.MemberStatus;
import com.tastyhouse.core.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.application.dto.MemberSearchCondition;
import com.tastyhouse.core.domain.member.application.dto.result.MemberListItemResult;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final JPAQueryFactory queryFactory;
    private final MemberJpaRepository memberJpaRepository;

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
    public PageResult<MemberWithProfileImageResult> findByNicknameContaining(String nickname, PageQuery pageQuery) {
        List<MemberWithProfileImageResult> content = queryFactory
            .select(Projections.constructor(MemberWithProfileImageResult.class,
                memberJpaEntity.id,
                memberJpaEntity.nickname,
                memberJpaEntity.memberGrade,
                memberJpaEntity.statusMessage,
                uploadedFile.filePath
            ))
            .from(memberJpaEntity)
            .leftJoin(uploadedFile).on(memberJpaEntity.profileImageFileId.eq(uploadedFile.id))
            .where(memberJpaEntity.nickname.containsIgnoreCase(nickname))
            .orderBy(memberJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Long total = queryFactory
            .select(memberJpaEntity.count())
            .from(memberJpaEntity)
            .where(memberJpaEntity.nickname.containsIgnoreCase(nickname))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
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
    public Optional<MemberWithProfileImageResult> findMemberWithProfileImageById(MemberId memberId) {
        return Optional.ofNullable(
            queryFactory
                .select(Projections.constructor(MemberWithProfileImageResult.class,
                    memberJpaEntity.id,
                    memberJpaEntity.nickname,
                    memberJpaEntity.memberGrade,
                    memberJpaEntity.statusMessage,
                    uploadedFile.filePath
                ))
                .from(memberJpaEntity)
                .leftJoin(uploadedFile).on(memberJpaEntity.profileImageFileId.eq(uploadedFile.id))
                .where(memberJpaEntity.id.eq(memberId.value()))
                .fetchOne()
        );
    }

    @Override
    public PageResult<MemberListItemResult> findMembers(MemberSearchCondition condition, PageQuery pageQuery) {
        List<MemberListItemResult> content = queryFactory
            .select(Projections.constructor(MemberListItemResult.class,
                memberJpaEntity.id,
                memberJpaEntity.username,
                memberJpaEntity.nickname,
                memberJpaEntity.fullName,
                memberJpaEntity.phoneNumber.value,
                memberJpaEntity.gender,
                memberJpaEntity.memberGrade,
                memberJpaEntity.memberStatus,
                uploadedFile.filePath,
                memberJpaEntity.createdAt
            ))
            .from(memberJpaEntity)
            .leftJoin(uploadedFile).on(memberJpaEntity.profileImageFileId.eq(uploadedFile.id))
            .where(
                nicknameContains(condition.nickname()),
                usernameContains(condition.username()),
                phoneContains(condition.phone()),
                statusEq(condition.status()),
                gradeEq(condition.grade())
            )
            .orderBy(memberJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Long total = queryFactory
            .select(memberJpaEntity.count())
            .from(memberJpaEntity)
            .where(
                nicknameContains(condition.nickname()),
                usernameContains(condition.username()),
                phoneContains(condition.phone()),
                statusEq(condition.status()),
                gradeEq(condition.grade())
            )
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Member save(Member member) {
        if (member.getId() == null) {
            MemberJpaEntity saved = memberJpaRepository.save(MemberMapper.toEntity(member));
            return MemberMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        MemberJpaEntity entity = memberJpaRepository.findById(member.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 회원입니다: " + member.getId()));
        MemberMapper.applyChanges(entity, member);
        return MemberMapper.toDomain(entity);
    }

    private BooleanExpression nicknameContains(String nickname) {
        return StringUtils.hasText(nickname) ? memberJpaEntity.nickname.containsIgnoreCase(nickname) : null;
    }

    private BooleanExpression usernameContains(String username) {
        return StringUtils.hasText(username) ? memberJpaEntity.username.containsIgnoreCase(username) : null;
    }

    private BooleanExpression phoneContains(String phone) {
        return StringUtils.hasText(phone) ? memberJpaEntity.phoneNumber.value.containsIgnoreCase(phone) : null;
    }

    private BooleanExpression statusEq(MemberStatus status) {
        return status != null ? memberJpaEntity.memberStatus.eq(status) : null;
    }

    private BooleanExpression gradeEq(MemberGrade grade) {
        return grade != null ? memberJpaEntity.memberGrade.eq(grade) : null;
    }
}
