package com.tastyhouse.application.member.port.out.write;

import java.util.List;
import java.util.Optional;

public interface MemberStatePort {
    Optional<MemberState> findById(Long memberId);

    Optional<MemberState> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByNickname(String nickname);

    Optional<MemberState> findByNickname(String nickname);

    boolean existsByPhoneNumberAndStatusNot(String phoneNumber, String memberStatus);

    Optional<MemberState> findByPhoneNumberAndStatusNot(String phoneNumber, String memberStatus);

    long bulkUpdateGrade(List<Long> memberIds, String grade);

    MemberState save(MemberState state);
}
