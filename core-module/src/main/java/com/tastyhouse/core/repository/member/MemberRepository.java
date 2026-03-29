package com.tastyhouse.core.repository.member;

import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.entity.user.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository {

    Optional<Member> findById(Long memberId);

    boolean existsById(Long memberId);

    List<Member> findAllById(Collection<Long> memberIds);

    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    Optional<Member> findByNickname(String nickname);

    Page<Member> findByNicknameContaining(String nickname, Pageable pageable);

    boolean existsByPhoneNumberValueAndMemberStatusNot(String phoneNumber, MemberStatus memberStatus);

    Page<Member> findAllMembers(Pageable pageable);
}
