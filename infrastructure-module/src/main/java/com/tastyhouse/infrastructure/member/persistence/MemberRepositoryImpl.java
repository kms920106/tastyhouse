package com.tastyhouse.infrastructure.member.persistence;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.tastyhouse.domain.member.domain.model.Member;
import com.tastyhouse.domain.member.domain.model.MemberGrade;
import com.tastyhouse.domain.member.domain.model.MemberStatus;
import com.tastyhouse.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.domain.member.domain.vo.MemberId;

import static com.tastyhouse.infrastructure.member.persistence.QMemberJpaEntity.memberJpaEntity;

/**
 * 회원 write 어댑터.
 *
 * <p>단건 로드·중복 검증·등급 일괄 갱신·저장만 담당한다. 표현 목적 read(회원 관리 목록·닉네임 검색·
 * 프로필 이미지 조인 투영)는 같은 모듈의 {@code member/query/MemberQueryDao}로 이관했다.
 */
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
    public Map<Long, String> findNicknamesByIds(Collection<Long> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Map.of();
        }

        List<Long> distinctIds = memberIds.stream().distinct().toList();

        List<Tuple> tuples = queryFactory
            .select(memberJpaEntity.id, memberJpaEntity.nickname)
            .from(memberJpaEntity)
            .where(memberJpaEntity.id.in(distinctIds))
            .fetch();

        Map<Long, String> nicknamesById = new HashMap<>();
        for (Tuple tuple : tuples) {
            Long id = tuple.get(memberJpaEntity.id);
            String nickname = tuple.get(memberJpaEntity.nickname);
            if (id == null || nickname == null) {
                continue;
            }
            nicknamesById.putIfAbsent(id, nickname);
        }
        return nicknamesById;
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
}
