package com.tastyhouse.core.domain.member.domain.repository;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.member.domain.model.MemberGrade;
import com.tastyhouse.core.domain.member.domain.model.MemberStatus;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.application.dto.MemberSearchCondition;
import com.tastyhouse.core.domain.member.application.dto.result.MemberListItemResult;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface MemberRepository {

    Optional<Member> findById(MemberId memberId);

    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    Optional<Member> findByNickname(String nickname);

    PageResult<MemberWithProfileImageResult> findByNicknameContaining(String nickname, PageQuery pageQuery);

    boolean existsByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus);

    Optional<Member> findByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus);

    long bulkUpdateGrade(List<Long> memberIds, MemberGrade grade);

    Optional<MemberWithProfileImageResult> findMemberWithProfileImageById(MemberId memberId);

    PageResult<MemberListItemResult> findMembers(MemberSearchCondition condition, PageQuery pageQuery);

    Member save(Member member);
}
