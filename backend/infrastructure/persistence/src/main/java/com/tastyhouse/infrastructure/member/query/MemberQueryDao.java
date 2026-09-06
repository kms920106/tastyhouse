package com.tastyhouse.infrastructure.member.query;

import com.tastyhouse.application.member.port.out.MemberManagementDetailResult;
import com.tastyhouse.application.member.port.out.MemberPersonalInfoResult;
import com.tastyhouse.application.member.port.out.MemberManagementQueryPort;
import com.tastyhouse.application.member.port.out.MemberQueryPort;
import com.tastyhouse.application.member.port.out.MemberListItemResult;
import com.tastyhouse.application.member.port.out.MemberSearchCondition;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.ConstructorExpression;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;

@Repository
public class MemberQueryDao implements MemberQueryPort, MemberManagementQueryPort {
    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public MemberQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
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
                uploadedFileJpaEntity.filePath,
                memberJpaEntity.createdAt
            ))
            .from(memberJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
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
            .fetch()
            .stream()
            .map(this::withResolvedProfileImageUrl)
            .toList();

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
    public PageResult<MemberWithProfileImageResult> findByNicknameContaining(String nickname, PageQuery pageQuery) {
        List<MemberWithProfileImageResult> content = queryFactory
            .select(memberWithProfileImageProjection())
            .from(memberJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .where(memberJpaEntity.nickname.containsIgnoreCase(nickname))
            .orderBy(memberJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedProfileImageUrl)
            .toList();

        Long total = queryFactory
            .select(memberJpaEntity.count())
            .from(memberJpaEntity)
            .where(memberJpaEntity.nickname.containsIgnoreCase(nickname))
            .fetchOne();

        return PageResult.of(content, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<MemberWithProfileImageResult> findMemberWithProfileImageById(MemberId memberId) {
        return Optional.ofNullable(
                queryFactory
                    .select(memberWithProfileImageProjection())
                    .from(memberJpaEntity)
                    .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
                    .where(memberJpaEntity.id.eq(memberId.value()))
                    .fetchOne()
            )
            .map(this::withResolvedProfileImageUrl);
    }

    @Override
    public Optional<String> findProfileImageUrl(MemberId memberId) {
        String filePath = queryFactory
            .select(uploadedFileJpaEntity.filePath)
            .from(memberJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .where(memberJpaEntity.id.eq(memberId.value()))
            .fetchOne();

        return Optional.ofNullable(fileUrlResolver.resolve(filePath));
    }

    @Override
    public Map<Long, MemberWithProfileImageResult> findMemberWithProfileImagesByIds(Collection<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        List<Long> distinctIds = memberIds.stream().distinct().toList();

        return queryFactory
            .select(memberWithProfileImageProjection())
            .from(memberJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(memberProfileImageFileId().eq(uploadedFileJpaEntity.id))
            .where(memberJpaEntity.id.in(distinctIds))
            .fetch()
            .stream()
            .map(this::withResolvedProfileImageUrl)
            .collect(Collectors.toMap(
                MemberWithProfileImageResult::id,
                Function.identity(),
                (existing, replacement) -> existing
            ));
    }

    private ConstructorExpression<MemberWithProfileImageResult> memberWithProfileImageProjection() {
        return Projections.constructor(MemberWithProfileImageResult.class,
                memberJpaEntity.id,
            memberJpaEntity.nickname,
            memberJpaEntity.memberGrade,
            memberJpaEntity.statusMessage,
            uploadedFileJpaEntity.filePath
        );
    }

    private MemberListItemResult withResolvedProfileImageUrl(MemberListItemResult row) {
        return new MemberListItemResult(
            row.id(),
            row.username(),
            row.nickname(),
            row.fullName(),
            row.phoneNumber(),
            row.gender(),
            row.memberGrade(),
            row.memberStatus(),
            fileUrlResolver.resolve(row.profileImageUrl()),
            row.createdAt()
        );
    }

    private MemberWithProfileImageResult withResolvedProfileImageUrl(MemberWithProfileImageResult row) {
        return new MemberWithProfileImageResult(
            row.id(),
            row.nickname(),
            row.memberGrade(),
            row.statusMessage(),
            fileUrlResolver.resolve(row.profileImageUrl())
        );
    }

    private NumberPath<Long> memberProfileImageFileId() {
        return Expressions.numberPath(Long.class, memberJpaEntity, "profileImageFileId");
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

    @Override
    public Optional<MemberPersonalInfoResult> findPersonalInfoById(MemberId memberId) {
        MemberPersonalInfoResult result = queryFactory
            .select(Projections.constructor(MemberPersonalInfoResult.class,
                memberJpaEntity.username,
                memberJpaEntity.fullName,
                memberJpaEntity.phoneNumber.value,
                memberJpaEntity.birthDate,
                memberJpaEntity.gender.stringValue(),
                memberJpaEntity.pushNotificationEnabled,
                memberJpaEntity.marketingInfoEnabled,
                memberJpaEntity.eventInfoEnabled
            ))
            .from(memberJpaEntity)
            .where(memberJpaEntity.id.eq(memberId.value()))
            .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        Integer found = queryFactory
            .selectOne()
            .from(memberJpaEntity)
            .where(memberJpaEntity.nickname.eq(nickname))
            .fetchFirst();

        return found != null;
    }

    @Override
    public boolean existsByActivePhoneNumber(String phoneNumber) {
        Integer found = queryFactory
            .selectOne()
            .from(memberJpaEntity)
            .where(
                memberJpaEntity.phoneNumber.value.eq(phoneNumber),
                memberJpaEntity.memberStatus.ne(MemberStatus.DELETED)
            )
            .fetchFirst();

        return found != null;
    }

    @Override
    public Optional<MemberManagementDetailResult> findManagementDetailById(MemberId memberId) {
        MemberManagementDetailResult result = queryFactory
            .select(Projections.constructor(MemberManagementDetailResult.class,
                memberJpaEntity.id,
                memberJpaEntity.username,
                memberJpaEntity.nickname,
                memberJpaEntity.fullName,
                memberJpaEntity.phoneNumber.value,
                memberJpaEntity.gender.stringValue(),
                memberJpaEntity.birthDate,
                memberJpaEntity.memberGrade.stringValue(),
                memberJpaEntity.memberStatus.stringValue(),
                memberJpaEntity.statusMessage,
                memberJpaEntity.pushNotificationEnabled,
                memberJpaEntity.marketingInfoEnabled,
                memberJpaEntity.eventInfoEnabled,
                memberJpaEntity.createdAt
            ))
            .from(memberJpaEntity)
            .where(memberJpaEntity.id.eq(memberId.value()))
            .fetchOne();

        return Optional.ofNullable(result);
    }
}
