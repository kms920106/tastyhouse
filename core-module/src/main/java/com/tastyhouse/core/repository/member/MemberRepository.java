package com.tastyhouse.core.repository.member;

import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.entity.user.MemberGrade;
import com.tastyhouse.core.entity.user.MemberStatus;
import com.tastyhouse.core.entity.user.dto.MemberProfileDetailDto;
import com.tastyhouse.core.entity.user.dto.MemberWithProfileImageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Optional<Member> findById(Long memberId);

    boolean existsById(Long memberId);

    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    Optional<Member> findByNickname(String nickname);

    Page<MemberWithProfileImageDto> findByNicknameContaining(String nickname, Pageable pageable);

    boolean existsByPhoneNumberValueAndMemberStatusNot(String phoneNumber, MemberStatus memberStatus);

    Optional<Member> findByPhoneNumberValueAndMemberStatusNot(String phoneNumber, MemberStatus memberStatus);

    long bulkUpdateGrade(List<Long> memberIds, MemberGrade grade);

    Optional<MemberWithProfileImageDto> findMemberWithProfileImageById(Long memberId);

    Optional<MemberProfileDetailDto> findMemberProfileDetailById(Long memberId);
}
