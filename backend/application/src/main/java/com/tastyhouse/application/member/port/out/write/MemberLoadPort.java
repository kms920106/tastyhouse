package com.tastyhouse.application.member.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.vo.MemberId;

public interface MemberLoadPort {

    Optional<Member> findById(MemberId memberId);

    Optional<Member> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    Optional<Member> findByNickname(String nickname);

    boolean existsByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus);

    Optional<Member> findByPhoneNumberAndStatusNot(String phoneNumber, MemberStatus memberStatus);
}
