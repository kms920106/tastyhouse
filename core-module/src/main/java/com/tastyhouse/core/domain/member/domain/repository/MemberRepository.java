package com.tastyhouse.core.domain.member.domain.repository;

import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.member.domain.model.MemberGrade;
import com.tastyhouse.core.domain.member.domain.model.MemberStatus;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Optional<Member> findById(MemberId memberId);

    boolean existsById(MemberId memberId);

    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    Optional<Member> findByNickname(String nickname);

    Page<MemberWithProfileImageResult> findByNicknameContaining(String nickname, Pageable pageable);

    boolean existsByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus);

    Optional<Member> findByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus);

    long bulkUpdateGrade(List<Long> memberIds, MemberGrade grade);

    Optional<MemberWithProfileImageResult> findMemberWithProfileImageById(MemberId memberId);

    Member save(Member member);
}
